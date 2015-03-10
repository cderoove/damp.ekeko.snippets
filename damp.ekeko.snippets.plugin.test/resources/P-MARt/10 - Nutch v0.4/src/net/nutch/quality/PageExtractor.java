/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.quality;

import java.io.*;
import java.net.*;
import java.util.*;

import net.nutch.io.*;
import net.nutch.searcher.*;
import net.nutch.net.protocols.http.*;
import net.nutch.quality.dynamic.*;

/*********************************************
 * The PageExtractor creates a PageDescription from the
 * indicated file, then uses it to extract the info 
 * from a downloaded HTML page.
 *
 *********************************************/
public class PageExtractor {
    /**
     * Lets us abstract the differences between a remote
     * search engine and Nutch
     */
    public static interface IExtractor {
        ArrayList applyQuery(String query) throws IOException;
    }

    /**
     * An IExtractor wrapper for PageExtractor
     */
    public static class RemotePageExtractor implements IExtractor {
        PageExtractor pageExtractor;

        /**
         */
        public RemotePageExtractor(File pageDesc, String userAgent, boolean debug) throws IOException, ParseException {
            pageExtractor = new PageExtractor(pageDesc, userAgent, debug);
        }

        /**
         * The Remote Engine searcher will return a list of
         * HashMap items, each of which could hold a number of
         * fields.  We're only interested in the "href" one.
         */
        public ArrayList applyQuery(String query) throws IOException {
            ArrayList results = pageExtractor.applyQuery(query);
            if (results == null) {
                return results;
            }

            ArrayList strResults = new ArrayList();
            for (Iterator it = results.iterator(); it.hasNext(); ) {
                HashMap hashmap = (HashMap) it.next();
                String val = (String) hashmap.get("href");
                strResults.add(val);
            }
            return strResults;
        }
    }

    /**
     * A local segment-searcher that will return search queries
     */
    public static class NutchExtractor implements IExtractor {
        private NutchBean searcher;
        /**
         */
        public NutchExtractor(String dir) throws IOException {
          searcher = new NutchBean(new File(dir));
        }

        /**
         */
        public ArrayList applyQuery(String queryStr) throws IOException {
            ArrayList results = new ArrayList();

            Query query = Query.parse(queryStr);
            Hits hits = searcher.search(query, 10);
            long max = Math.min(hits.getTotal(), 10);
            for (int i = 0; i < max; i++) {
              HitDetails details = searcher.getDetails(hits.getHit(i));
              results.add(details.getValue("url"));
            }
            return results;
        }
    }

    boolean debug;
    String userAgent;
    PageDescription desc;
    Http http;
    URL url;

    /**
     */
    public PageExtractor(File pageDesc, String userAgent, boolean debug) throws IOException, ParseException {
        this.debug = debug;
        this.userAgent = userAgent;
        this.http = new Http();
        http.setAgentString(userAgent);
        http.setAgentEmail("");
        http.setTimeout(10000);

        InputStream in = new FileInputStream(pageDesc);
        try {
            this.desc = new PageDescription(in);
            desc.parse();
        } finally {
            in.close();
        }
    }

    /**
     * Apply the query, and parse out the results using the Page
     * Description.
     */
    public ArrayList applyQuery(String query) throws IOException {
        ArrayList interprets = desc.getInterprets();
        HashMap curInterpret = null;
        String page = getPage(query);

        if (debug) {
            System.err.println(page);
        }

        String interpretRegion = null;
        if (interprets.size() == 0) {
            interpretRegion = getResultList(page, new HashMap());
        } else {
            for (Iterator it = interprets.iterator(); it.hasNext(); ) {
                curInterpret = (HashMap) it.next();
                interpretRegion = getResultList(page, curInterpret);
                if (interpretRegion != null) {
                    break;
                }
            }
        }

        //
        // Apply the interpret directive to the found region
        //
        if (interpretRegion != null) {
            ArrayList items = new ArrayList();
            String itemStart = (String) curInterpret.get("resultitemstart");
            boolean trimItemStart = !"true".equalsIgnoreCase((String) curInterpret.get("keepitemstart"));
            if (itemStart == null) {
                itemStart = "HREF=";
            }

            String itemEnd = (String) curInterpret.get("resultitemend");
            boolean trimItemEnd = false;
            if (itemEnd == null) {
                itemEnd = itemStart;
                trimItemEnd = true;
            }

            //
            // Go through the content, looking for "itemStart" strings.
            //
            for (int start = page.indexOf(itemStart); start != -1; start = page.indexOf(itemStart, start)) {
                int itemEndIndex;

                if (trimItemStart) {
                    start += itemStart.length();
                    itemEndIndex = page.indexOf(itemEnd, start);
                } else {
                    itemEndIndex = page.indexOf(itemEnd, start+itemStart.length());
                }

                if (itemEndIndex < 0) {
                    itemEndIndex = page.length();
                } else if (!trimItemEnd) {
                    itemEndIndex += itemEnd.length();
                }

                String resultItem = page.substring(start, itemEndIndex).trim();
                items.add(parseResultItem(resultItem, curInterpret));
                start = itemEndIndex;
            }

            return items;
        }
        return null;
    }

    /**
     * This uses the query string and the PageDescriptor to
     * contact the server and fetch a page of content.  This
     * page is returned as a String.
     *
     * We will still need to extract the relevant fields.
     */
    private String getPage(String query) throws IOException {
        // First, build the HTTP Connection
        HashMap values = desc.getValues();

        String action = (String) values.get("action");
        String method = (String) values.get("method");
        String fullQuery = buildQueryString(query);

        this.url = new URL(action + fullQuery);
        
        //HttpURLConnection con = (HttpURLConnection) url.openConnection();
        // How to handle 'method' here?
        try {
          return new String(http.getResponse(url).getContent());
        } catch (HttpException e) {
          throw new IOException("HttpException: " + e.getMessage());
        }
    }

    /**
     */
    private String buildQueryString(String query) {
        StringBuffer queryString = new StringBuffer();
        queryString.append("?");

        ArrayList inputs = desc.getInputs();
        int count = 0;
        for (Iterator it = inputs.iterator(); it.hasNext(); count++) {
            HashMap input = (HashMap) it.next();
            if ("browser".equals((String) input.get("mode"))) {
                continue;				  // not for us
            }

            String name = (String) input.get("name");
            if (name == null) {
                throw new RuntimeException("input has no name: " + input);
            }

            if (count != 0) {
                queryString.append("&");
            }
            queryString.append(name);
            queryString.append("=");
            if (input.containsKey("user")) {
                try {
                    queryString.append(URLEncoder.encode(query, "UTF-8"));
                } catch (UnsupportedEncodingException uee) {
                }
            } else {
                String value = (String) input.get("value");
                if (value != null) {
                    queryString.append(value);
                }
            }
        }

        return queryString.toString();
    }

    /**
     */
    private HashMap parseResultItem(String html, HashMap interpret) {
        String extractArg = (String) interpret.get("extractarg");
        HashMap item = new HashMap();
        item.put("html", html.trim());
    
        int hrefStart = indexOfIgnoreCase("href=", html, 0);
        if (hrefStart != -1) {
            int hrefEnd = html.indexOf(">", hrefStart+5);
            if (hrefEnd != -1) {
                String href = html.substring(hrefStart+5,hrefEnd);
                href = trimQuotes(href, '\"');
                href = trimQuotes(href, '\'');
                href = href.trim();
                if (!href.startsWith("http:")) {
                    try {
                        href = new URL(url, href).toString();
                    } catch (MalformedURLException e) {
                    }
                }

                //
                // Sometimes what we want is embedded in another
                // URL.  Use this to extract it.
                //
                if (extractArg != null) {
                    int argIndex = href.indexOf("?" + extractArg + "=");
                    if (argIndex < 0) {
                        argIndex = href.indexOf("&" + extractArg + "=");
                    }
                    if (argIndex >= 0) {
                        int end = href.indexOf("&", argIndex + 1);
                        if (end < 0) {
                            end = href.length();
                        }
                        href = href.substring(argIndex + extractArg.length() + 2, end);
                        // Remove escaped chars, if any
                        try {
                            href = URLDecoder.decode(href, "utf-8");
                        } catch (UnsupportedEncodingException uee) {
                            uee.printStackTrace();
                        }
                    }
                }
                item.put("href", href.trim());
	
                int anchorStart = hrefEnd+1;
                int anchorEnd = indexOfIgnoreCase("</a>", html, anchorStart);
                if (anchorEnd != -1)
                    item.put("anchor", html.substring(anchorStart,anchorEnd).trim());
            }
        }
    
        getItemSection(item, html, "relevance", interpret, "relevancestart", "relevanceend");
        getItemSection(item, html, "price", interpret, "pricestart", "priceend");
        getItemSection(item, html, "avail", interpret, "availstart", "availend");
        getItemSection(item, html, "date", interpret, "datestart", "dateend");
        getItemSection(item, html, "name", interpret, "namestart", "nameend");
        getItemSection(item, html, "email", interpret, "emailstart", "emailend");
        return item;
    }

    /**
     */
    private void getItemSection(HashMap item, String html, String name, HashMap interpret, String start, String end) {
        String section = getSection(html, interpret, start, end);
        if (section != null) {
            item.put(name, removeTags(section).trim());
        }
    }

    /**
     */
    private String getSection(String text, HashMap interpret, String startName, String endName) {
        String start = (String) interpret.get(startName);
        String end = (String) interpret.get(endName);
        if (start != null && end != null) {
            int startIndex = text.indexOf(start);
            if (startIndex != -1) {
                int endIndex = text.indexOf(end, startIndex + start.length());
                if (endIndex != -1)
                    return text.substring(startIndex+start.length(), endIndex);
            }
        }
        return null;
    }

    /**
     */
    private int indexOfIgnoreCase(String pattern, String text, int start) {
        int patternLength = pattern.length();
        int end = text.length() - patternLength;
        for (int i = start; i <= end; i++) {
            if (text.regionMatches(true, i, pattern, 0, patternLength))
                return i;
        }
        return -1;
    }

    /**
     */
    private String trimQuotes(String href, char quote) {
        int quoteStart = href.indexOf(quote);
        if (quoteStart != -1) {
            int quoteEnd = href.indexOf(quote, quoteStart+1);
            if (quoteEnd != -1)
                return href.substring(quoteStart+1, quoteEnd);
        }
        return href;
    }

    /**
     */
    private String removeTags(String html) {
        StringBuffer result = new StringBuffer();
        int start = 0;
        for (int i = html.indexOf('<'); i >= 0; i = html.indexOf('<', start)) {
            int j = html.indexOf('>', i+1);
            if (j < 0)
                break;
            result.append(html.substring(start, i));
            start = j+1;
        }
        if (start == 0) return html;
        result.append(html.substring(start, html.length()));
        return result.toString();
    }

    /**
     * Apply an Interpret set against the page contents.
     */
    private String getResultList(String page, HashMap interpret) {
        String start = (String) interpret.get("resultliststart");
        String end = (String) interpret.get("resultlistend");

        int startIndex = 0;
        int endIndex = page.length();
        if (start != null) {
            if ((startIndex = page.indexOf(start)) < 0) {
                return null;
            }
        }
        if (end != null) {
            if ((endIndex = page.indexOf(end, startIndex)) < 0) {
                return null;
            }
        }
        return page.substring(startIndex, endIndex);
    }

    /**
     * We emit stats 
     */
    public void emitStats() {
        HashMap descValues = desc.getValues();
        ArrayList inputs = desc.getInputs();
        ArrayList interprets = desc.getInterprets();

        System.out.println("Plugin name: " + (String) descValues.get("name"));
        System.out.println("Plugin URL: " + (String) descValues.get("url"));
        System.out.println("--------------------------------------------");
        for (Iterator it = descValues.keySet().iterator(); it.hasNext(); ) {
            String key = (String) it.next();
            System.out.println("  " + key + ": " + (String) descValues.get(key));
        }
        System.out.println();

        for (Iterator listKeys = inputs.iterator(); listKeys.hasNext(); ) {
            System.out.println("Inputs:");
            HashMap curValues = (HashMap) listKeys.next();

            for (Iterator it = curValues.keySet().iterator(); it.hasNext(); ) {
                String key = (String) it.next();
                System.out.println("  " + key + ": " + (String) curValues.get(key));
            }
            System.out.println();
        }
        System.out.println();

        for (Iterator listKeys = interprets.iterator(); listKeys.hasNext(); ) {
            System.out.println("Interprets:");
            HashMap curValues = (HashMap) listKeys.next();

            for (Iterator it = curValues.keySet().iterator(); it.hasNext(); ) {
                String key = (String) it.next();
                System.out.println("  " + key + ": " + (String) curValues.get(key));
            }
            System.out.println();
        }
    }

    /**
     */
    public static void main(String argv[]) throws IOException, ParseException {
        if (argv.length < 3) {
            System.out.println("Usage: java net.nutch.quality.PageExtractor <pageDesc> <userAgent> <query> [-debug]");
            return;
        }

        String pageDesc = argv[0];
        String userAgent = argv[1];
        String query = argv[2];
        boolean debug = false;
        if (argv.length > 3) {
            if ("-debug".equals(argv[3])) {
                debug = true;
            }
        }

        PageExtractor extractor = new PageExtractor(new File(pageDesc), userAgent, debug);

        ArrayList outs = extractor.applyQuery(query);
        if (outs == null) {
            System.out.println("Sorry, no results");
        } else {
            System.out.println("Number items: " + outs.size());
            System.out.println();
            for (Iterator it = outs.iterator(); it.hasNext(); ) {
                HashMap hashmap = (HashMap) it.next();
                String hit = (String) hashmap.get("href");
                System.out.println(hit);
            }
        }
    }
}
