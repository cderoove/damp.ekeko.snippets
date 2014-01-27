package damp.ekeko.snippets;

import java.io.IOException;
import java.util.Collection;
import java.util.IdentityHashMap;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import damp.ekeko.snippets.SnippetParser.DirectivesContext;
import damp.ekeko.snippets.SnippetParser.MetaContext;
import damp.ekeko.snippets.SnippetParser.PostmetaContext;
import damp.ekeko.snippets.SnippetParser.PremetaContext;

public class SnippetExtractor extends SnippetBaseVisitor<Void> {

	class SnippetBounds {
		public int openBracket;
		public int closeBracket;
		public int startAt;
		public int stopAt;
		public String directives;
		
		public String toString() {
			return "[=" + openBracket + " ]@=" + startAt + " [=" + stopAt + " ]= " + closeBracket + " -> " + directives;
			
		}
	}
	
	public Collection<SnippetBounds> getSnippetBounds() {
		return snippets.values();
	}
	
	public Collection<SnippetBounds> extractSnippetBounds(ParseTree tree) {
		tree.accept(this);
		return getSnippetBounds();
	}
	
	public IdentityHashMap<ParserRuleContext, SnippetBounds> snippets = new IdentityHashMap<ParserRuleContext, SnippetBounds>();
	
	@Override
	public Void visitMeta(MetaContext ctx) {
		SnippetBounds bounds = new SnippetBounds();
		snippets.put(ctx, bounds);
		return super.visitMeta(ctx);
	}
	
	@Override
	public Void visitPremeta(PremetaContext ctx) {
		SnippetBounds snippetBounds = snippets.get(ctx.getParent());
		
		TerminalNode open = ctx.OPEN();
		Token openToken = open.getSymbol();
		snippetBounds.openBracket = openToken.getStartIndex();
		
		TerminalNode middle = ctx.MIDDLE();
		Token middleToken = middle.getSymbol();
		snippetBounds.startAt = middleToken.getStartIndex();
		snippetBounds.stopAt = middleToken.getStopIndex();
		
		
		return super.visitPremeta(ctx);
	}
	
	@Override
	public Void visitDirectives(DirectivesContext ctx) {
		SnippetBounds snippetBounds = snippets.get(ctx.getParent().getParent());
		snippetBounds.directives = ctx.getText();
		return super.visitDirectives(ctx);
	}
	
	@Override
	public Void visitPostmeta(PostmetaContext ctx) {
		SnippetBounds snippetBounds = snippets.get(ctx.getParent());
		
		TerminalNode close = ctx.CLOSE();
		Token closeToken = close.getSymbol();
		snippetBounds.closeBracket = closeToken.getStartIndex();
				
		return super.visitPostmeta(ctx);
	}
	
	
	public static ExtractedSnippet extractSnippetBounds(String snippet) {
		ANTLRInputStream input = new ANTLRInputStream(snippet);
		SnippetLexer lexer = new SnippetLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		SnippetParser parser = new SnippetParser(tokens);
		ParseTree tree = parser.snippet(); 
		SnippetExtractor extractor = new SnippetExtractor();
		Collection<SnippetBounds> extractedSnippetBounds = extractor.extractSnippetBounds(tree);
		char[] characters = snippet.toCharArray();
		for(SnippetBounds sb : extractedSnippetBounds) {
			characters[sb.openBracket] = ' ';
			for(int i = sb.startAt; i <= sb.closeBracket; i++)
				characters[i] = ' ';
		}
		ExtractedSnippet extractedSnippet = new ExtractedSnippet();
		extractedSnippet.bounds = extractedSnippetBounds;
		extractedSnippet.snippet = new String(characters);
		return extractedSnippet;
	}
	
	
	
	public static void main(String[] args) throws IOException {

		ExtractedSnippet snippet = SnippetExtractor.extractSnippetBounds("[[int]@[foo] [x]@[relax]]@[dunno];");
		System.out.println(snippet.snippet);
		for(SnippetBounds bounds : snippet.bounds) 
			System.out.println(bounds);
	
	}

}
