/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

import java.io.IOException;
import antlr.TokenBuffer;
import antlr.LLkParser;
import antlr.Token;
import antlr.TokenStream;
import antlr.ParserException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.ParserSharedInputState;
import antlr.collections.impl.BitSet;
public class HTMLParser extends antlr.LLkParser
            implements HTMLTokenTypes
{

    protected HTMLParser(TokenBuffer tokenBuf, int k) {
        super(tokenBuf,k);
        tokenNames = _tokenNames;
    }

    public HTMLParser(TokenBuffer tokenBuf) {
        this(tokenBuf,1);
    }

    protected HTMLParser(TokenStream lexer, int k) {
        super(lexer,k);
        tokenNames = _tokenNames;
    }

    public HTMLParser(TokenStream lexer) {
        this(lexer,1);
    }

    public HTMLParser(ParserSharedInputState state) {
        super(state,1);
        tokenNames = _tokenNames;
    }

    public final void document() throws ParserException, IOException {


        try {      // for error handling
            {
                switch ( LA(1)) {
                case PCDATA:
                    {
                        match(PCDATA);
                        break;
                    }
                case EOF:
                case DOCTYPE:
                case OHTML:
                case CHTML:
                case OHEAD:
                case ISINDEX:
                case BASE:
                case META:
                case LINK:
                case OTITLE:
                case OSCRIPT:
                case OSTYLE:
                case OBODY:
                case ADDRESS:
                case HR:
                case IMG:
                case BFONT:
                case BR:
                case OH1:
                case OH2:
                case OH3:
                case OH4:
                case OH5:
                case OH6:
                case OPARA:
                case OULIST:
                case OOLIST:
                case ODLIST:
                case OPRE:
                case ODIV:
                case OCENTER:
                case OBQUOTE:
                case OFORM:
                case OTABLE:
                case OTTYPE:
                case OITALIC:
                case OBOLD:
                case OUNDER:
                case OSTRIKE:
                case OBIG:
                case OSMALL:
                case OSUB:
                case OSUP:
                case OEM:
                case OSTRONG:
                case ODEF:
                case OCODE:
                case OSAMP:
                case OKBD:
                case OVAR:
                case OCITE:
                case OANCHOR:
                case OAPPLET:
                case OFONT:
                case OMAP:
                    {
                        break;
                    }
                default:
                    {
                        throw new NoViableAltException(LT(1));
                    }
                }
            }
            {
                switch ( LA(1)) {
                case DOCTYPE:
                    {
                        match(DOCTYPE);
                        {
                            switch ( LA(1)) {
                            case PCDATA:
                                {
                                    match(PCDATA);
                                    break;
                                }
                            case EOF:
                            case OHTML:
                            case CHTML:
                            case OHEAD:
                            case ISINDEX:
                            case BASE:
                            case META:
                            case LINK:
                            case OTITLE:
                            case OSCRIPT:
                            case OSTYLE:
                            case OBODY:
                            case ADDRESS:
                            case HR:
                            case IMG:
                            case BFONT:
                            case BR:
                            case OH1:
                            case OH2:
                            case OH3:
                            case OH4:
                            case OH5:
                            case OH6:
                            case OPARA:
                            case OULIST:
                            case OOLIST:
                            case ODLIST:
                            case OPRE:
                            case ODIV:
                            case OCENTER:
                            case OBQUOTE:
                            case OFORM:
                            case OTABLE:
                            case OTTYPE:
                            case OITALIC:
                            case OBOLD:
                            case OUNDER:
                            case OSTRIKE:
                            case OBIG:
                            case OSMALL:
                            case OSUB:
                            case OSUP:
                            case OEM:
                            case OSTRONG:
                            case ODEF:
                            case OCODE:
                            case OSAMP:
                            case OKBD:
                            case OVAR:
                            case OCITE:
                            case OANCHOR:
                            case OAPPLET:
                            case OFONT:
                            case OMAP:
                                {
                                    break;
                                }
                            default:
                                {
                                    throw new NoViableAltException(LT(1));
                                }
                            }
                        }
                        break;
                    }
                case EOF:
                case OHTML:
                case CHTML:
                case OHEAD:
                case ISINDEX:
                case BASE:
                case META:
                case LINK:
                case OTITLE:
                case OSCRIPT:
                case OSTYLE:
                case OBODY:
                case ADDRESS:
                case HR:
                case IMG:
                case BFONT:
                case BR:
                case OH1:
                case OH2:
                case OH3:
                case OH4:
                case OH5:
                case OH6:
                case OPARA:
                case OULIST:
                case OOLIST:
                case ODLIST:
                case OPRE:
                case ODIV:
                case OCENTER:
                case OBQUOTE:
                case OFORM:
                case OTABLE:
                case OTTYPE:
                case OITALIC:
                case OBOLD:
                case OUNDER:
                case OSTRIKE:
                case OBIG:
                case OSMALL:
                case OSUB:
                case OSUP:
                case OEM:
                case OSTRONG:
                case ODEF:
                case OCODE:
                case OSAMP:
                case OKBD:
                case OVAR:
                case OCITE:
                case OANCHOR:
                case OAPPLET:
                case OFONT:
                case OMAP:
                    {
                        break;
                    }
                default:
                    {
                        throw new NoViableAltException(LT(1));
                    }
                }
            }
            {
                switch ( LA(1)) {
                case OHTML:
                    {
                        match(OHTML);
                        {
                            switch ( LA(1)) {
                            case PCDATA:
                                {
                                    match(PCDATA);
                                    break;
                                }
                            case EOF:
                            case CHTML:
                            case OHEAD:
                            case ISINDEX:
                            case BASE:
                            case META:
                            case LINK:
                            case OTITLE:
                            case OSCRIPT:
                            case OSTYLE:
                            case OBODY:
                            case ADDRESS:
                            case HR:
                            case IMG:
                            case BFONT:
                            case BR:
                            case OH1:
                            case OH2:
                            case OH3:
                            case OH4:
                            case OH5:
                            case OH6:
                            case OPARA:
                            case OULIST:
                            case OOLIST:
                            case ODLIST:
                            case OPRE:
                            case ODIV:
                            case OCENTER:
                            case OBQUOTE:
                            case OFORM:
                            case OTABLE:
                            case OTTYPE:
                            case OITALIC:
                            case OBOLD:
                            case OUNDER:
                            case OSTRIKE:
                            case OBIG:
                            case OSMALL:
                            case OSUB:
                            case OSUP:
                            case OEM:
                            case OSTRONG:
                            case ODEF:
                            case OCODE:
                            case OSAMP:
                            case OKBD:
                            case OVAR:
                            case OCITE:
                            case OANCHOR:
                            case OAPPLET:
                            case OFONT:
                            case OMAP:
                                {
                                    break;
                                }
                            default:
                                {
                                    throw new NoViableAltException(LT(1));
                                }
                            }
                        }
                        break;
                    }
                case EOF:
                case CHTML:
                case OHEAD:
                case ISINDEX:
                case BASE:
                case META:
                case LINK:
                case OTITLE:
                case OSCRIPT:
                case OSTYLE:
                case OBODY:
                case ADDRESS:
                case HR:
                case IMG:
                case BFONT:
                case BR:
                case OH1:
                case OH2:
                case OH3:
                case OH4:
                case OH5:
                case OH6:
                case OPARA:
                case OULIST:
                case OOLIST:
                case ODLIST:
                case OPRE:
                case ODIV:
                case OCENTER:
                case OBQUOTE:
                case OFORM:
                case OTABLE:
                case OTTYPE:
                case OITALIC:
                case OBOLD:
                case OUNDER:
                case OSTRIKE:
                case OBIG:
                case OSMALL:
                case OSUB:
                case OSUP:
                case OEM:
                case OSTRONG:
                case ODEF:
                case OCODE:
                case OSAMP:
                case OKBD:
                case OVAR:
                case OCITE:
                case OANCHOR:
                case OAPPLET:
                case OFONT:
                case OMAP:
                    {
                        break;
                    }
                default:
                    {
                        throw new NoViableAltException(LT(1));
                    }
                }
            }
            {
                switch ( LA(1)) {
                case OHEAD:
                case ISINDEX:
                case BASE:
                case META:
                case LINK:
                case OTITLE:
                case OSCRIPT:
                case OSTYLE:
                    {
                        head();
                        break;
                    }
                case EOF:
                case CHTML:
                case OBODY:
                case ADDRESS:
                case HR:
                case IMG:
                case BFONT:
                case BR:
                case OH1:
                case OH2:
                case OH3:
                case OH4:
                case OH5:
                case OH6:
                case OPARA:
                case OULIST:
                case OOLIST:
                case ODLIST:
                case OPRE:
                case ODIV:
                case OCENTER:
                case OBQUOTE:
                case OFORM:
                case OTABLE:
                case OTTYPE:
                case OITALIC:
                case OBOLD:
                case OUNDER:
                case OSTRIKE:
                case OBIG:
                case OSMALL:
                case OSUB:
                case OSUP:
                case OEM:
                case OSTRONG:
                case ODEF:
                case OCODE:
                case OSAMP:
                case OKBD:
                case OVAR:
                case OCITE:
                case OANCHOR:
                case OAPPLET:
                case OFONT:
                case OMAP:
                    {
                        break;
                    }
                default:
                    {
                        throw new NoViableAltException(LT(1));
                    }
                }
            }
            {
                switch ( LA(1)) {
                case OBODY:
                case ADDRESS:
                case HR:
                case IMG:
                case BFONT:
                case BR:
                case OH1:
                case OH2:
                case OH3:
                case OH4:
                case OH5:
                case OH6:
                case OPARA:
                case OULIST:
                case OOLIST:
                case ODLIST:
                case OPRE:
                case ODIV:
                case OCENTER:
                case OBQUOTE:
                case OFORM:
                case OTABLE:
                case OTTYPE:
                case OITALIC:
                case OBOLD:
                case OUNDER:
                case OSTRIKE:
                case OBIG:
                case OSMALL:
                case OSUB:
                case OSUP:
                case OEM:
                case OSTRONG:
                case ODEF:
                case OCODE:
                case OSAMP:
                case OKBD:
                case OVAR:
                case OCITE:
                case OANCHOR:
                case OAPPLET:
                case OFONT:
                case OMAP:
                    {
                        body();
                        break;
                    }
                case EOF:
                case CHTML:
                    {
                        break;
                    }
                default:
                    {
                        throw new NoViableAltException(LT(1));
                    }
                }
            }
            {
                switch ( LA(1)) {
                case CHTML:
                    {
                        match(CHTML);
                        {
                            switch ( LA(1)) {
                            case PCDATA:
                                {
                                    match(PCDATA);
                                    break;
                                }
                            case EOF:
                                {
                                    break;
                                }
                            default:
                                {
                                    throw new NoViableAltException(LT(1));
                                }
                            }
                        }
                        break;
                    }
                case EOF:
                    {
                        break;
                    }
                default:
                    {
                        throw new NoViableAltException(LT(1));
                    }
                }
            }
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_0);
        }
    }

    public final void head() throws ParserException, IOException {


        try {      // for error handling
            {
                switch ( LA(1)) {
                case OHEAD:
                    {
                        match(OHEAD);
                        {
                            switch ( LA(1)) {
                            case PCDATA:
                                {
                                    match(PCDATA);
                                    break;
                                }
                            case ISINDEX:
                            case BASE:
                            case META:
                            case LINK:
                            case OTITLE:
                            case OSCRIPT:
                            case OSTYLE:
                                {
                                    break;
                                }
                            default:
                                {
                                    throw new NoViableAltException(LT(1));
                                }
                            }
                        }
                        break;
                    }
                case ISINDEX:
                case BASE:
                case META:
                case LINK:
                case OTITLE:
                case OSCRIPT:
                case OSTYLE:
                    {
                        break;
                    }
                default:
                    {
                        throw new NoViableAltException(LT(1));
                    }
                }
            }
            head_element();
            {
_loop15:
                do {
                    switch ( LA(1)) {
                    case PCDATA:
                        {
                            match(PCDATA);
                            break;
                        }
                    case ISINDEX:
                    case BASE:
                    case META:
                    case LINK:
                    case OTITLE:
                    case OSCRIPT:
                    case OSTYLE:
                        {
                            head_element();
                            break;
                        }
                    default:
                        {
                            break _loop15;
                        }
                    }
                } while (true);
            }
            {
                switch ( LA(1)) {
                case CHEAD:
                    {
                        match(CHEAD);
                        {
                            switch ( LA(1)) {
                            case PCDATA:
                                {
                                    match(PCDATA);
                                    break;
                                }
                            case EOF:
                            case CHTML:
                            case OBODY:
                            case ADDRESS:
                            case HR:
                            case IMG:
                            case BFONT:
                            case BR:
                            case OH1:
                            case OH2:
                            case OH3:
                            case OH4:
                            case OH5:
                            case OH6:
                            case OPARA:
                            case OULIST:
                            case OOLIST:
                            case ODLIST:
                            case OPRE:
                            case ODIV:
                            case OCENTER:
                            case OBQUOTE:
                            case OFORM:
                            case OTABLE:
                            case OTTYPE:
                            case OITALIC:
                            case OBOLD:
                            case OUNDER:
                            case OSTRIKE:
                            case OBIG:
                            case OSMALL:
                            case OSUB:
                            case OSUP:
                            case OEM:
                            case OSTRONG:
                            case ODEF:
                            case OCODE:
                            case OSAMP:
                            case OKBD:
                            case OVAR:
                            case OCITE:
                            case OANCHOR:
                            case OAPPLET:
                            case OFONT:
                            case OMAP:
                                {
                                    break;
                                }
                            default:
                                {
                                    throw new NoViableAltException(LT(1));
                                }
                            }
                        }
                        break;
                    }
                case EOF:
                case CHTML:
                case OBODY:
                case ADDRESS:
                case HR:
                case IMG:
                case BFONT:
                case BR:
                case OH1:
                case OH2:
                case OH3:
                case OH4:
                case OH5:
                case OH6:
                case OPARA:
                case OULIST:
                case OOLIST:
                case ODLIST:
                case OPRE:
                case ODIV:
                case OCENTER:
                case OBQUOTE:
                case OFORM:
                case OTABLE:
                case OTTYPE:
                case OITALIC:
                case OBOLD:
                case OUNDER:
                case OSTRIKE:
                case OBIG:
                case OSMALL:
                case OSUB:
                case OSUP:
                case OEM:
                case OSTRONG:
                case ODEF:
                case OCODE:
                case OSAMP:
                case OKBD:
                case OVAR:
                case OCITE:
                case OANCHOR:
                case OAPPLET:
                case OFONT:
                case OMAP:
                    {
                        break;
                    }
                default:
                    {
                        throw new NoViableAltException(LT(1));
                    }
                }
            }
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_1);
        }
    }

    public final void body() throws ParserException, IOException {


        try {      // for error handling
            {
                switch ( LA(1)) {
                case OBODY:
                    {
                        match(OBODY);
                        {
_loop30:
                            do {
                                if ((LA(1)==PCDATA)) {
                                    match(PCDATA);
                                }
                                else {
                                    break _loop30;
                                }

                            } while (true);
                        }
                        break;
                    }
                case ADDRESS:
                case HR:
                case IMG:
                case BFONT:
                case BR:
                case OH1:
                case OH2:
                case OH3:
                case OH4:
                case OH5:
                case OH6:
                case OPARA:
                case OULIST:
                case OOLIST:
                case ODLIST:
                case OPRE:
                case ODIV:
                case OCENTER:
                case OBQUOTE:
                case OFORM:
                case OTABLE:
                case OTTYPE:
                case OITALIC:
                case OBOLD:
                case OUNDER:
                case OSTRIKE:
                case OBIG:
                case OSMALL:
                case OSUB:
                case OSUP:
                case OEM:
                case OSTRONG:
                case ODEF:
                case OCODE:
                case OSAMP:
                case OKBD:
                case OVAR:
                case OCITE:
                case OANCHOR:
                case OAPPLET:
                case OFONT:
                case OMAP:
                    {
                        break;
                    }
                default:
                    {
                        throw new NoViableAltException(LT(1));
                    }
                }
            }
            body_content_no_PCDATA();
            {
                int _cnt32=0;
_loop32:
                do {
                    if ((_tokenSet_2.member(LA(1)))) {
                        body_content();
                    }
                    else {
                        if ( _cnt32>=1 ) { break _loop32; } else {throw new NoViableAltException(LT(1));}
                    }

                    _cnt32++;
                } while (true);
            }
            {
                switch ( LA(1)) {
                case CBODY:
                    {
                        match(CBODY);
                        {
_loop35:
                            do {
                                if ((LA(1)==PCDATA)) {
                                    match(PCDATA);
                                }
                                else {
                                    break _loop35;
                                }

                            } while (true);
                        }
                        break;
                    }
                case EOF:
                case CHTML:
                    {
                        break;
                    }
                default:
                    {
                        throw new NoViableAltException(LT(1));
                    }
                }
            }
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_3);
        }
    }

    public final void head_element() throws ParserException, IOException {


        try {      // for error handling
            switch ( LA(1)) {
            case OTITLE:
                {
                    title();
                    break;
                }
            case OSCRIPT:
                {
                    script();
                    break;
                }
            case OSTYLE:
                {
                    style();
                    break;
                }
            case ISINDEX:
                {
                    match(ISINDEX);
                    break;
                }
            case BASE:
                {
                    match(BASE);
                    break;
                }
            case META:
                {
                    match(META);
                    break;
                }
            case LINK:
                {
                    match(LINK);
                    break;
                }
            default:
                {
                    throw new NoViableAltException(LT(1));
                }
            }
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_4);
        }
    }

    public final void title() throws ParserException, IOException {


        try {      // for error handling
            match(OTITLE);
            {
                switch ( LA(1)) {
                case PCDATA:
                    {
                        match(PCDATA);
                        break;
                    }
                case CTITLE:
                    {
                        break;
                    }
                default:
                    {
                        throw new NoViableAltException(LT(1));
                    }
                }
            }
            match(CTITLE);
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_4);
        }
    }

    public final void script() throws ParserException, IOException {


        try {      // for error handling
            match(OSCRIPT);
            {
                int _cnt23=0;
_loop23:
                do {
                    if ((_tokenSet_5.member(LA(1)))) {
                        matchNot(CSCRIPT);
                    }
                    else {
                        if ( _cnt23>=1 ) { break _loop23; } else {throw new NoViableAltException(LT(1));}
                    }

                    _cnt23++;
                } while (true);
            }
            match(CSCRIPT);
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_4);
        }
    }

    public final void style() throws ParserException, IOException {


        try {      // for error handling
            match(OSTYLE);
            {
                int _cnt26=0;
_loop26:
                do {
                    if ((_tokenSet_6.member(LA(1)))) {
                        matchNot(CSTYLE);
                    }
                    else {
                        if ( _cnt26>=1 ) { break _loop26; } else {throw new NoViableAltException(LT(1));}
                    }

                    _cnt26++;
                } while (true);
            }
            match(CSTYLE);
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_4);
        }
    }

    public final void body_content_no_PCDATA() throws ParserException, IOException {


        try {      // for error handling
            switch ( LA(1)) {
            case ADDRESS:
            case HR:
            case OH1:
            case OH2:
            case OH3:
            case OH4:
            case OH5:
            case OH6:
            case OPARA:
            case OULIST:
            case OOLIST:
            case ODLIST:
            case OPRE:
            case ODIV:
            case OCENTER:
            case OBQUOTE:
            case OTABLE:
                {
                    body_tag();
                    break;
                }
            case IMG:
            case BFONT:
            case BR:
            case OFORM:
            case OTTYPE:
            case OITALIC:
            case OBOLD:
            case OUNDER:
            case OSTRIKE:
            case OBIG:
            case OSMALL:
            case OSUB:
            case OSUP:
            case OEM:
            case OSTRONG:
            case ODEF:
            case OCODE:
            case OSAMP:
            case OKBD:
            case OVAR:
            case OCITE:
            case OANCHOR:
            case OAPPLET:
            case OFONT:
            case OMAP:
                {
                    text_tag();
                    break;
                }
            default:
                {
                    throw new NoViableAltException(LT(1));
                }
            }
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_2);
        }
    }

    public final void body_content() throws ParserException, IOException {


        try {      // for error handling
            switch ( LA(1)) {
            case ADDRESS:
            case HR:
            case OH1:
            case OH2:
            case OH3:
            case OH4:
            case OH5:
            case OH6:
            case OPARA:
            case OULIST:
            case OOLIST:
            case ODLIST:
            case OPRE:
            case ODIV:
            case OCENTER:
            case OBQUOTE:
            case OTABLE:
                {
                    body_tag();
                    break;
                }
            case PCDATA:
            case IMG:
            case BFONT:
            case BR:
            case OFORM:
            case OTTYPE:
            case OITALIC:
            case OBOLD:
            case OUNDER:
            case OSTRIKE:
            case OBIG:
            case OSMALL:
            case OSUB:
            case OSUP:
            case OEM:
            case OSTRONG:
            case ODEF:
            case OCODE:
            case OSAMP:
            case OKBD:
            case OVAR:
            case OCITE:
            case OANCHOR:
            case OAPPLET:
            case OFONT:
            case OMAP:
                {
                    text();
                    break;
                }
            default:
                {
                    throw new NoViableAltException(LT(1));
                }
            }
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_7);
        }
    }

    public final void body_tag() throws ParserException, IOException {


        try {      // for error handling
            switch ( LA(1)) {
            case OH1:
            case OH2:
            case OH3:
            case OH4:
            case OH5:
            case OH6:
                {
                    heading();
                    break;
                }
            case HR:
            case OPARA:
            case OULIST:
            case OOLIST:
            case ODLIST:
            case OPRE:
            case ODIV:
            case OCENTER:
            case OBQUOTE:
            case OTABLE:
                {
                    block();
                    break;
                }
            case ADDRESS:
                {
                    match(ADDRESS);
                    break;
                }
            default:
                {
                    throw new NoViableAltException(LT(1));
                }
            }
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_7);
        }
    }

    public final void text_tag() throws ParserException, IOException {


        try {      // for error handling
            switch ( LA(1)) {
            case OTTYPE:
            case OITALIC:
            case OBOLD:
            case OUNDER:
            case OSTRIKE:
            case OBIG:
            case OSMALL:
            case OSUB:
            case OSUP:
                {
                    font();
                    break;
                }
            case OEM:
            case OSTRONG:
            case ODEF:
            case OCODE:
            case OSAMP:
            case OKBD:
            case OVAR:
            case OCITE:
                {
                    phrase();
                    break;
                }
            case IMG:
            case BFONT:
            case BR:
            case OANCHOR:
            case OAPPLET:
            case OFONT:
            case OMAP:
                {
                    special();
                    break;
                }
            case OFORM:
                {
                    form();
                    break;
                }
            default:
                {
                    throw new NoViableAltException(LT(1));
                }
            }
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_8);
        }
    }

    public final void heading() throws ParserException, IOException {


        try {      // for error handling
            switch ( LA(1)) {
            case OH1:
                {
                    h1();
                    break;
                }
            case OH2:
                {
                    h2();
                    break;
                }
            case OH3:
                {
                    h3();
                    break;
                }
            case OH4:
                {
                    h4();
                    break;
                }
            case OH5:
                {
                    h5();
                    break;
                }
            case OH6:
                {
                    h6();
                    break;
                }
            default:
                {
                    throw new NoViableAltException(LT(1));
                }
            }
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_7);
        }
    }

    public final void block() throws ParserException, IOException {


        try {      // for error handling
            switch ( LA(1)) {
            case OPARA:
                {
                    paragraph();
                    break;
                }
            case OULIST:
            case OOLIST:
            case ODLIST:
                {
                    list();
                    break;
                }
            case OPRE:
                {
                    preformatted();
                    break;
                }
            case ODIV:
                {
                    div();
                    break;
                }
            case OCENTER:
                {
                    center();
                    break;
                }
            case OBQUOTE:
                {
                    blockquote();
                    break;
                }
            case HR:
                {
                    match(HR);
                    break;
                }
            case OTABLE:
                {
                    table();
                    break;
                }
            default:
                {
                    throw new NoViableAltException(LT(1));
                }
            }
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_9);
        }
    }

    public final void text() throws ParserException, IOException {


        try {      // for error handling
            switch ( LA(1)) {
            case PCDATA:
                {
                    match(PCDATA);
                    break;
                }
            case IMG:
            case BFONT:
            case BR:
            case OFORM:
            case OTTYPE:
            case OITALIC:
            case OBOLD:
            case OUNDER:
            case OSTRIKE:
            case OBIG:
            case OSMALL:
            case OSUB:
            case OSUP:
            case OEM:
            case OSTRONG:
            case ODEF:
            case OCODE:
            case OSAMP:
            case OKBD:
            case OVAR:
            case OCITE:
            case OANCHOR:
            case OAPPLET:
            case OFONT:
            case OMAP:
                {
                    text_tag();
                    break;
                }
            default:
                {
                    throw new NoViableAltException(LT(1));
                }
            }
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_8);
        }
    }

    public final void h1() throws ParserException, IOException {


        try {      // for error handling
            match(OH1);
            {
_loop48:
                do {
                    switch ( LA(1)) {
                    case HR:
                    case OPARA:
                    case OULIST:
                    case OOLIST:
                    case ODLIST:
                    case OPRE:
                    case ODIV:
                    case OCENTER:
                    case OBQUOTE:
                    case OTABLE:
                        {
                            block();
                            break;
                        }
                    case PCDATA:
                    case IMG:
                    case BFONT:
                    case BR:
                    case OFORM:
                    case OTTYPE:
                    case OITALIC:
                    case OBOLD:
                    case OUNDER:
                    case OSTRIKE:
                    case OBIG:
                    case OSMALL:
                    case OSUB:
                    case OSUP:
                    case OEM:
                    case OSTRONG:
                    case ODEF:
                    case OCODE:
                    case OSAMP:
                    case OKBD:
                    case OVAR:
                    case OCITE:
                    case OANCHOR:
                    case OAPPLET:
                    case OFONT:
                    case OMAP:
                        {
                            text();
                            break;
                        }
                    default:
                        {
                            break _loop48;
                        }
                    }
                } while (true);
            }
            match(CH1);
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_7);
        }
    }

    public final void h2() throws ParserException, IOException {


        try {      // for error handling
            match(OH2);
            {
_loop51:
                do {
                    switch ( LA(1)) {
                    case HR:
                    case OPARA:
                    case OULIST:
                    case OOLIST:
                    case ODLIST:
                    case OPRE:
                    case ODIV:
                    case OCENTER:
                    case OBQUOTE:
                    case OTABLE:
                        {
                            block();
                            break;
                        }
                    case PCDATA:
                    case IMG:
                    case BFONT:
                    case BR:
                    case OFORM:
                    case OTTYPE:
                    case OITALIC:
                    case OBOLD:
                    case OUNDER:
                    case OSTRIKE:
                    case OBIG:
                    case OSMALL:
                    case OSUB:
                    case OSUP:
                    case OEM:
                    case OSTRONG:
                    case ODEF:
                    case OCODE:
                    case OSAMP:
                    case OKBD:
                    case OVAR:
                    case OCITE:
                    case OANCHOR:
                    case OAPPLET:
                    case OFONT:
                    case OMAP:
                        {
                            text();
                            break;
                        }
                    default:
                        {
                            break _loop51;
                        }
                    }
                } while (true);
            }
            match(CH2);
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_7);
        }
    }

    public final void h3() throws ParserException, IOException {


        try {      // for error handling
            match(OH3);
            {
_loop54:
                do {
                    switch ( LA(1)) {
                    case HR:
                    case OPARA:
                    case OULIST:
                    case OOLIST:
                    case ODLIST:
                    case OPRE:
                    case ODIV:
                    case OCENTER:
                    case OBQUOTE:
                    case OTABLE:
                        {
                            block();
                            break;
                        }
                    case PCDATA:
                    case IMG:
                    case BFONT:
                    case BR:
                    case OFORM:
                    case OTTYPE:
                    case OITALIC:
                    case OBOLD:
                    case OUNDER:
                    case OSTRIKE:
                    case OBIG:
                    case OSMALL:
                    case OSUB:
                    case OSUP:
                    case OEM:
                    case OSTRONG:
                    case ODEF:
                    case OCODE:
                    case OSAMP:
                    case OKBD:
                    case OVAR:
                    case OCITE:
                    case OANCHOR:
                    case OAPPLET:
                    case OFONT:
                    case OMAP:
                        {
                            text();
                            break;
                        }
                    default:
                        {
                            break _loop54;
                        }
                    }
                } while (true);
            }
            match(CH3);
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_7);
        }
    }

    public final void h4() throws ParserException, IOException {


        try {      // for error handling
            match(OH4);
            {
_loop57:
                do {
                    switch ( LA(1)) {
                    case HR:
                    case OPARA:
                    case OULIST:
                    case OOLIST:
                    case ODLIST:
                    case OPRE:
                    case ODIV:
                    case OCENTER:
                    case OBQUOTE:
                    case OTABLE:
                        {
                            block();
                            break;
                        }
                    case PCDATA:
                    case IMG:
                    case BFONT:
                    case BR:
                    case OFORM:
                    case OTTYPE:
                    case OITALIC:
                    case OBOLD:
                    case OUNDER:
                    case OSTRIKE:
                    case OBIG:
                    case OSMALL:
                    case OSUB:
                    case OSUP:
                    case OEM:
                    case OSTRONG:
                    case ODEF:
                    case OCODE:
                    case OSAMP:
                    case OKBD:
                    case OVAR:
                    case OCITE:
                    case OANCHOR:
                    case OAPPLET:
                    case OFONT:
                    case OMAP:
                        {
                            text();
                            break;
                        }
                    default:
                        {
                            break _loop57;
                        }
                    }
                } while (true);
            }
            match(CH4);
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_7);
        }
    }

    public final void h5() throws ParserException, IOException {


        try {      // for error handling
            match(OH5);
            {
_loop60:
                do {
                    switch ( LA(1)) {
                    case HR:
                    case OPARA:
                    case OULIST:
                    case OOLIST:
                    case ODLIST:
                    case OPRE:
                    case ODIV:
                    case OCENTER:
                    case OBQUOTE:
                    case OTABLE:
                        {
                            block();
                            break;
                        }
                    case PCDATA:
                    case IMG:
                    case BFONT:
                    case BR:
                    case OFORM:
                    case OTTYPE:
                    case OITALIC:
                    case OBOLD:
                    case OUNDER:
                    case OSTRIKE:
                    case OBIG:
                    case OSMALL:
                    case OSUB:
                    case OSUP:
                    case OEM:
                    case OSTRONG:
                    case ODEF:
                    case OCODE:
                    case OSAMP:
                    case OKBD:
                    case OVAR:
                    case OCITE:
                    case OANCHOR:
                    case OAPPLET:
                    case OFONT:
                    case OMAP:
                        {
                            text();
                            break;
                        }
                    default:
                        {
                            break _loop60;
                        }
                    }
                } while (true);
            }
            match(CH5);
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_7);
        }
    }

    public final void h6() throws ParserException, IOException {


        try {      // for error handling
            match(OH6);
            {
_loop63:
                do {
                    switch ( LA(1)) {
                    case HR:
                    case OPARA:
                    case OULIST:
                    case OOLIST:
                    case ODLIST:
                    case OPRE:
                    case ODIV:
                    case OCENTER:
                    case OBQUOTE:
                    case OTABLE:
                        {
                            block();
                            break;
                        }
                    case PCDATA:
                    case IMG:
                    case BFONT:
                    case BR:
                    case OFORM:
                    case OTTYPE:
                    case OITALIC:
                    case OBOLD:
                    case OUNDER:
                    case OSTRIKE:
                    case OBIG:
                    case OSMALL:
                    case OSUB:
                    case OSUP:
                    case OEM:
                    case OSTRONG:
                    case ODEF:
                    case OCODE:
                    case OSAMP:
                    case OKBD:
                    case OVAR:
                    case OCITE:
                    case OANCHOR:
                    case OAPPLET:
                    case OFONT:
                    case OMAP:
                        {
                            text();
                            break;
                        }
                    default:
                        {
                            break _loop63;
                        }
                    }
                } while (true);
            }
            match(CH6);
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_7);
        }
    }

    public final void paragraph() throws ParserException, IOException {


        try {      // for error handling
            match(OPARA);
            {
_loop69:
                do {
                    if ((_tokenSet_10.member(LA(1)))) {
                        text();
                    }
                    else {
                        break _loop69;
                    }

                } while (true);
            }
            {
                switch ( LA(1)) {
                case CPARA:
                    {
                        match(CPARA);
                        break;
                    }
                case EOF:
                case PCDATA:
                case CHTML:
                case CBODY:
                case ADDRESS:
                case HR:
                case IMG:
                case BFONT:
                case BR:
                case OH1:
                case CH1:
                case OH2:
                case CH2:
                case OH3:
                case CH3:
                case OH4:
                case CH4:
                case OH5:
                case CH5:
                case OH6:
                case CH6:
                case OPARA:
                case OULIST:
                case OOLIST:
                case ODLIST:
                case CDTERM:
                case OPRE:
                case ODIV:
                case CDIV:
                case OCENTER:
                case CCENTER:
                case OBQUOTE:
                case OFORM:
                case CFORM:
                case OTABLE:
                case CTABLE:
                case O_TR:
                case C_TR:
                case O_TH_OR_TD:
                case C_TH_OR_TD:
                case OTTYPE:
                case OITALIC:
                case OBOLD:
                case OUNDER:
                case OSTRIKE:
                case OBIG:
                case OSMALL:
                case OSUB:
                case OSUP:
                case OEM:
                case OSTRONG:
                case ODEF:
                case OCODE:
                case OSAMP:
                case OKBD:
                case OVAR:
                case OCITE:
                case INPUT:
                case OSELECT:
                case OTAREA:
                case OANCHOR:
                case OAPPLET:
                case OFONT:
                case OMAP:
                    {
                        break;
                    }
                default:
                    {
                        throw new NoViableAltException(LT(1));
                    }
                }
            }
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_9);
        }
    }

    public final void list() throws ParserException, IOException {


        try {      // for error handling
            switch ( LA(1)) {
            case OULIST:
                {
                    unordered_list();
                    break;
                }
            case OOLIST:
                {
                    ordered_list();
                    break;
                }
            case ODLIST:
                {
                    def_list();
                    break;
                }
            default:
                {
                    throw new NoViableAltException(LT(1));
                }
            }
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_11);
        }
    }

    public final void preformatted() throws ParserException, IOException {


        try {      // for error handling
            match(OPRE);
            {
                int _cnt112=0;
_loop112:
                do {
                    if ((_tokenSet_10.member(LA(1)))) {
                        text();
                    }
                    else {
                        if ( _cnt112>=1 ) { break _loop112; } else {throw new NoViableAltException(LT(1));}
                    }

                    _cnt112++;
                } while (true);
            }
            match(CPRE);
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_9);
        }
    }

    public final void div() throws ParserException, IOException {


        try {      // for error handling
            match(ODIV);
            {
_loop115:
                do {
                    if ((_tokenSet_2.member(LA(1)))) {
                        body_content();
                    }
                    else {
                        break _loop115;
                    }

                } while (true);
            }
            match(CDIV);
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_9);
        }
    }

    public final void center() throws ParserException, IOException {


        try {      // for error handling
            match(OCENTER);
            {
_loop118:
                do {
                    if ((_tokenSet_2.member(LA(1)))) {
                        body_content();
                    }
                    else {
                        break _loop118;
                    }

                } while (true);
            }
            match(CCENTER);
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_9);
        }
    }

    public final void blockquote() throws ParserException, IOException {


        try {      // for error handling
            match(OBQUOTE);
            match(PCDATA);
            match(CBQUOTE);
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_9);
        }
    }

    public final void table() throws ParserException, IOException {


        try {      // for error handling
            match(OTABLE);
            {
                switch ( LA(1)) {
                case OCAP:
                    {
                        caption();
                        break;
                    }
                case PCDATA:
                case O_TR:
                    {
                        break;
                    }
                default:
                    {
                        throw new NoViableAltException(LT(1));
                    }
                }
            }
            {
_loop126:
                do {
                    if ((LA(1)==PCDATA)) {
                        match(PCDATA);
                    }
                    else {
                        break _loop126;
                    }

                } while (true);
            }
            {
                int _cnt128=0;
_loop128:
                do {
                    if ((LA(1)==O_TR)) {
                        tr();
                    }
                    else {
                        if ( _cnt128>=1 ) { break _loop128; } else {throw new NoViableAltException(LT(1));}
                    }

                    _cnt128++;
                } while (true);
            }
            match(CTABLE);
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_9);
        }
    }

    public final void font() throws ParserException, IOException {


        try {      // for error handling
            switch ( LA(1)) {
            case OTTYPE:
                {
                    teletype();
                    break;
                }
            case OITALIC:
                {
                    italic();
                    break;
                }
            case OBOLD:
                {
                    bold();
                    break;
                }
            case OUNDER:
                {
                    underline();
                    break;
                }
            case OSTRIKE:
                {
                    strike();
                    break;
                }
            case OBIG:
                {
                    big();
                    break;
                }
            case OSMALL:
                {
                    small();
                    break;
                }
            case OSUB:
                {
                    subscript();
                    break;
                }
            case OSUP:
                {
                    superscript();
                    break;
                }
            default:
                {
                    throw new NoViableAltException(LT(1));
                }
            }
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_8);
        }
    }

    public final void teletype() throws ParserException, IOException {


        try {      // for error handling
            match(OTTYPE);
            {
                int _cnt148=0;
_loop148:
                do {
                    if ((_tokenSet_10.member(LA(1)))) {
                        text();
                    }
                    else {
                        if ( _cnt148>=1 ) { break _loop148; } else {throw new NoViableAltException(LT(1));}
                    }

                    _cnt148++;
                } while (true);
            }
            match(CTTYPE);
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_8);
        }
    }

    public final void italic() throws ParserException, IOException {


        try {      // for error handling
            match(OITALIC);
            {
                int _cnt151=0;
_loop151:
                do {
                    if ((_tokenSet_10.member(LA(1)))) {
                        text();
                    }
                    else {
                        if ( _cnt151>=1 ) { break _loop151; } else {throw new NoViableAltException(LT(1));}
                    }

                    _cnt151++;
                } while (true);
            }
            match(CITALIC);
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_8);
        }
    }

    public final void bold() throws ParserException, IOException {


        try {      // for error handling
            match(OBOLD);
            {
                int _cnt154=0;
_loop154:
                do {
                    if ((_tokenSet_10.member(LA(1)))) {
                        text();
                    }
                    else {
                        if ( _cnt154>=1 ) { break _loop154; } else {throw new NoViableAltException(LT(1));}
                    }

                    _cnt154++;
                } while (true);
            }
            match(CBOLD);
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_8);
        }
    }

    public final void underline() throws ParserException, IOException {


        try {      // for error handling
            match(OUNDER);
            {
                int _cnt157=0;
_loop157:
                do {
                    if ((_tokenSet_10.member(LA(1)))) {
                        text();
                    }
                    else {
                        if ( _cnt157>=1 ) { break _loop157; } else {throw new NoViableAltException(LT(1));}
                    }

                    _cnt157++;
                } while (true);
            }
            match(CUNDER);
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_8);
        }
    }

    public final void strike() throws ParserException, IOException {


        try {      // for error handling
            match(OSTRIKE);
            {
                int _cnt160=0;
_loop160:
                do {
                    if ((_tokenSet_10.member(LA(1)))) {
                        text();
                    }
                    else {
                        if ( _cnt160>=1 ) { break _loop160; } else {throw new NoViableAltException(LT(1));}
                    }

                    _cnt160++;
                } while (true);
            }
            match(CSTRIKE);
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_8);
        }
    }

    public final void big() throws ParserException, IOException {


        try {      // for error handling
            match(OBIG);
            {
                int _cnt163=0;
_loop163:
                do {
                    if ((_tokenSet_10.member(LA(1)))) {
                        text();
                    }
                    else {
                        if ( _cnt163>=1 ) { break _loop163; } else {throw new NoViableAltException(LT(1));}
                    }

                    _cnt163++;
                } while (true);
            }
            match(CBIG);
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_8);
        }
    }

    public final void small() throws ParserException, IOException {


        try {      // for error handling
            match(OSMALL);
            {
                int _cnt166=0;
_loop166:
                do {
                    if ((_tokenSet_10.member(LA(1)))) {
                        text();
                    }
                    else {
                        if ( _cnt166>=1 ) { break _loop166; } else {throw new NoViableAltException(LT(1));}
                    }

                    _cnt166++;
                } while (true);
            }
            match(CSMALL);
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_8);
        }
    }

    public final void subscript() throws ParserException, IOException {


        try {      // for error handling
            match(OSUB);
            {
                int _cnt169=0;
_loop169:
                do {
                    if ((_tokenSet_10.member(LA(1)))) {
                        text();
                    }
                    else {
                        if ( _cnt169>=1 ) { break _loop169; } else {throw new NoViableAltException(LT(1));}
                    }

                    _cnt169++;
                } while (true);
            }
            match(CSUB);
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_8);
        }
    }

    public final void superscript() throws ParserException, IOException {


        try {      // for error handling
            match(OSUP);
            {
                int _cnt172=0;
_loop172:
                do {
                    if ((_tokenSet_10.member(LA(1)))) {
                        text();
                    }
                    else {
                        if ( _cnt172>=1 ) { break _loop172; } else {throw new NoViableAltException(LT(1));}
                    }

                    _cnt172++;
                } while (true);
            }
            match(CSUP);
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_8);
        }
    }

    public final void phrase() throws ParserException, IOException {


        try {      // for error handling
            switch ( LA(1)) {
            case OEM:
                {
                    emphasize();
                    break;
                }
            case OSTRONG:
                {
                    strong();
                    break;
                }
            case ODEF:
                {
                    definition();
                    break;
                }
            case OCODE:
                {
                    code();
                    break;
                }
            case OSAMP:
                {
                    sample_output();
                    break;
                }
            case OKBD:
                {
                    keyboard_text();
                    break;
                }
            case OVAR:
                {
                    variable();
                    break;
                }
            case OCITE:
                {
                    citation();
                    break;
                }
            default:
                {
                    throw new NoViableAltException(LT(1));
                }
            }
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_8);
        }
    }

    public final void emphasize() throws ParserException, IOException {


        try {      // for error handling
            match(OEM);
            {
                int _cnt175=0;
_loop175:
                do {
                    if ((_tokenSet_10.member(LA(1)))) {
                        text();
                    }
                    else {
                        if ( _cnt175>=1 ) { break _loop175; } else {throw new NoViableAltException(LT(1));}
                    }

                    _cnt175++;
                } while (true);
            }
            match(CEM);
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_8);
        }
    }

    public final void strong() throws ParserException, IOException {


        try {      // for error handling
            match(OSTRONG);
            {
                int _cnt178=0;
_loop178:
                do {
                    if ((_tokenSet_10.member(LA(1)))) {
                        text();
                    }
                    else {
                        if ( _cnt178>=1 ) { break _loop178; } else {throw new NoViableAltException(LT(1));}
                    }

                    _cnt178++;
                } while (true);
            }
            match(CSTRONG);
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_8);
        }
    }

    public final void definition() throws ParserException, IOException {


        try {      // for error handling
            match(ODEF);
            {
                int _cnt181=0;
_loop181:
                do {
                    if ((_tokenSet_10.member(LA(1)))) {
                        text();
                    }
                    else {
                        if ( _cnt181>=1 ) { break _loop181; } else {throw new NoViableAltException(LT(1));}
                    }

                    _cnt181++;
                } while (true);
            }
            match(CDEF);
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_8);
        }
    }

    public final void code() throws ParserException, IOException {


        try {      // for error handling
            match(OCODE);
            {
                int _cnt184=0;
_loop184:
                do {
                    if ((_tokenSet_10.member(LA(1)))) {
                        text();
                    }
                    else {
                        if ( _cnt184>=1 ) { break _loop184; } else {throw new NoViableAltException(LT(1));}
                    }

                    _cnt184++;
                } while (true);
            }
            match(CCODE);
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_8);
        }
    }

    public final void sample_output() throws ParserException, IOException {


        try {      // for error handling
            match(OSAMP);
            {
                int _cnt187=0;
_loop187:
                do {
                    if ((_tokenSet_10.member(LA(1)))) {
                        text();
                    }
                    else {
                        if ( _cnt187>=1 ) { break _loop187; } else {throw new NoViableAltException(LT(1));}
                    }

                    _cnt187++;
                } while (true);
            }
            match(CSAMP);
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_8);
        }
    }

    public final void keyboard_text() throws ParserException, IOException {


        try {      // for error handling
            match(OKBD);
            {
                int _cnt190=0;
_loop190:
                do {
                    if ((_tokenSet_10.member(LA(1)))) {
                        text();
                    }
                    else {
                        if ( _cnt190>=1 ) { break _loop190; } else {throw new NoViableAltException(LT(1));}
                    }

                    _cnt190++;
                } while (true);
            }
            match(CKBD);
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_8);
        }
    }

    public final void variable() throws ParserException, IOException {


        try {      // for error handling
            match(OVAR);
            {
                int _cnt193=0;
_loop193:
                do {
                    if ((_tokenSet_10.member(LA(1)))) {
                        text();
                    }
                    else {
                        if ( _cnt193>=1 ) { break _loop193; } else {throw new NoViableAltException(LT(1));}
                    }

                    _cnt193++;
                } while (true);
            }
            match(CVAR);
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_8);
        }
    }

    public final void citation() throws ParserException, IOException {


        try {      // for error handling
            match(OCITE);
            {
                int _cnt196=0;
_loop196:
                do {
                    if ((_tokenSet_10.member(LA(1)))) {
                        text();
                    }
                    else {
                        if ( _cnt196>=1 ) { break _loop196; } else {throw new NoViableAltException(LT(1));}
                    }

                    _cnt196++;
                } while (true);
            }
            match(CCITE);
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_8);
        }
    }

    public final void special() throws ParserException, IOException {


        try {      // for error handling
            switch ( LA(1)) {
            case OANCHOR:
                {
                    anchor();
                    break;
                }
            case IMG:
                {
                    match(IMG);
                    break;
                }
            case OAPPLET:
                {
                    applet();
                    break;
                }
            case OFONT:
                {
                    font_dfn();
                    break;
                }
            case BFONT:
                {
                    match(BFONT);
                    break;
                }
            case OMAP:
                {
                    map();
                    break;
                }
            case BR:
                {
                    match(BR);
                    break;
                }
            default:
                {
                    throw new NoViableAltException(LT(1));
                }
            }
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_8);
        }
    }

    public final void anchor() throws ParserException, IOException {


        try {      // for error handling
            match(OANCHOR);
            {
_loop211:
                do {
                    if ((_tokenSet_10.member(LA(1)))) {
                        text();
                    }
                    else {
                        break _loop211;
                    }

                } while (true);
            }
            match(CANCHOR);
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_8);
        }
    }

    public final void applet() throws ParserException, IOException {


        try {      // for error handling
            match(OAPPLET);
            {
                switch ( LA(1)) {
                case APARAM:
                    {
                        match(APARAM);
                        break;
                    }
                case PCDATA:
                case CAPPLET:
                    {
                        break;
                    }
                default:
                    {
                        throw new NoViableAltException(LT(1));
                    }
                }
            }
            {
_loop215:
                do {
                    if ((LA(1)==PCDATA)) {
                        match(PCDATA);
                    }
                    else {
                        break _loop215;
                    }

                } while (true);
            }
            match(CAPPLET);
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_8);
        }
    }

    public final void font_dfn() throws ParserException, IOException {


        try {      // for error handling
            match(OFONT);
            {
_loop218:
                do {
                    if ((_tokenSet_10.member(LA(1)))) {
                        text();
                    }
                    else {
                        break _loop218;
                    }

                } while (true);
            }
            match(CFONT);
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_8);
        }
    }

    public final void map() throws ParserException, IOException {


        try {      // for error handling
            match(OMAP);
            {
                int _cnt221=0;
_loop221:
                do {
                    if ((LA(1)==AREA)) {
                        match(AREA);
                    }
                    else {
                        if ( _cnt221>=1 ) { break _loop221; } else {throw new NoViableAltException(LT(1));}
                    }

                    _cnt221++;
                } while (true);
            }
            match(CMAP);
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_8);
        }
    }

    public final void form() throws ParserException, IOException {


        try {      // for error handling
            match(OFORM);
            {
_loop122:
                do {
                    switch ( LA(1)) {
                    case INPUT:
                    case OSELECT:
                    case OTAREA:
                        {
                            form_field();
                            break;
                        }
                    case PCDATA:
                    case ADDRESS:
                    case HR:
                    case IMG:
                    case BFONT:
                    case BR:
                    case OH1:
                    case OH2:
                    case OH3:
                    case OH4:
                    case OH5:
                    case OH6:
                    case OPARA:
                    case OULIST:
                    case OOLIST:
                    case ODLIST:
                    case OPRE:
                    case ODIV:
                    case OCENTER:
                    case OBQUOTE:
                    case OFORM:
                    case OTABLE:
                    case OTTYPE:
                    case OITALIC:
                    case OBOLD:
                    case OUNDER:
                    case OSTRIKE:
                    case OBIG:
                    case OSMALL:
                    case OSUB:
                    case OSUP:
                    case OEM:
                    case OSTRONG:
                    case ODEF:
                    case OCODE:
                    case OSAMP:
                    case OKBD:
                    case OVAR:
                    case OCITE:
                    case OANCHOR:
                    case OAPPLET:
                    case OFONT:
                    case OMAP:
                        {
                            body_content();
                            break;
                        }
                    default:
                        {
                            break _loop122;
                        }
                    }
                } while (true);
            }
            match(CFORM);
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_8);
        }
    }

    public final void address() throws ParserException, IOException {


        try {      // for error handling
            match(OADDRESS);
            {
_loop66:
                do {
                    if ((LA(1)==PCDATA)) {
                        match(PCDATA);
                    }
                    else {
                        break _loop66;
                    }

                } while (true);
            }
            match(CADDRESS);
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_0);
        }
    }

    public final void unordered_list() throws ParserException, IOException {


        try {      // for error handling
            match(OULIST);
            {
_loop74:
                do {
                    if ((LA(1)==PCDATA)) {
                        match(PCDATA);
                    }
                    else {
                        break _loop74;
                    }

                } while (true);
            }
            {
                int _cnt76=0;
_loop76:
                do {
                    if ((LA(1)==OLITEM)) {
                        list_item();
                    }
                    else {
                        if ( _cnt76>=1 ) { break _loop76; } else {throw new NoViableAltException(LT(1));}
                    }

                    _cnt76++;
                } while (true);
            }
            match(CULIST);
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_11);
        }
    }

    public final void ordered_list() throws ParserException, IOException {


        try {      // for error handling
            match(OOLIST);
            {
_loop79:
                do {
                    if ((LA(1)==PCDATA)) {
                        match(PCDATA);
                    }
                    else {
                        break _loop79;
                    }

                } while (true);
            }
            {
                int _cnt81=0;
_loop81:
                do {
                    if ((LA(1)==OLITEM)) {
                        list_item();
                    }
                    else {
                        if ( _cnt81>=1 ) { break _loop81; } else {throw new NoViableAltException(LT(1));}
                    }

                    _cnt81++;
                } while (true);
            }
            match(COLIST);
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_11);
        }
    }

    public final void def_list() throws ParserException, IOException {


        try {      // for error handling
            match(ODLIST);
            {
_loop84:
                do {
                    if ((LA(1)==PCDATA)) {
                        match(PCDATA);
                    }
                    else {
                        break _loop84;
                    }

                } while (true);
            }
            {
                int _cnt86=0;
_loop86:
                do {
                    if ((LA(1)==ODTERM||LA(1)==ODDEF)) {
                        def_list_item();
                    }
                    else {
                        if ( _cnt86>=1 ) { break _loop86; } else {throw new NoViableAltException(LT(1));}
                    }

                    _cnt86++;
                } while (true);
            }
            match(CDLIST);
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_11);
        }
    }

    public final void list_item() throws ParserException, IOException {


        try {      // for error handling
            match(OLITEM);
            {
                int _cnt89=0;
_loop89:
                do {
                    switch ( LA(1)) {
                    case PCDATA:
                    case IMG:
                    case BFONT:
                    case BR:
                    case OFORM:
                    case OTTYPE:
                    case OITALIC:
                    case OBOLD:
                    case OUNDER:
                    case OSTRIKE:
                    case OBIG:
                    case OSMALL:
                    case OSUB:
                    case OSUP:
                    case OEM:
                    case OSTRONG:
                    case ODEF:
                    case OCODE:
                    case OSAMP:
                    case OKBD:
                    case OVAR:
                    case OCITE:
                    case OANCHOR:
                    case OAPPLET:
                    case OFONT:
                    case OMAP:
                        {
                            text();
                            break;
                        }
                    case OULIST:
                    case OOLIST:
                    case ODLIST:
                        {
                            list();
                            break;
                        }
                    default:
                        {
                            if ( _cnt89>=1 ) { break _loop89; } else {throw new NoViableAltException(LT(1));}
                        }
                    }
                    _cnt89++;
                } while (true);
            }
            {
                switch ( LA(1)) {
                case CLITEM:
                    {
                        match(CLITEM);
                        {
_loop92:
                            do {
                                if ((LA(1)==PCDATA)) {
                                    match(PCDATA);
                                }
                                else {
                                    break _loop92;
                                }

                            } while (true);
                        }
                        break;
                    }
                case CULIST:
                case COLIST:
                case OLITEM:
                case CDIR:
                case CMENU:
                    {
                        break;
                    }
                default:
                    {
                        throw new NoViableAltException(LT(1));
                    }
                }
            }
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_12);
        }
    }

    public final void def_list_item() throws ParserException, IOException {


        try {      // for error handling
            switch ( LA(1)) {
            case ODTERM:
                {
                    dt();
                    break;
                }
            case ODDEF:
                {
                    dd();
                    break;
                }
            default:
                {
                    throw new NoViableAltException(LT(1));
                }
            }
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_13);
        }
    }

    public final void dt() throws ParserException, IOException {


        try {      // for error handling
            match(ODTERM);
            {
                int _cnt96=0;
_loop96:
                do {
                    if ((_tokenSet_10.member(LA(1)))) {
                        text();
                    }
                    else {
                        if ( _cnt96>=1 ) { break _loop96; } else {throw new NoViableAltException(LT(1));}
                    }

                    _cnt96++;
                } while (true);
            }
            match(CDTERM);
            {
_loop98:
                do {
                    if ((LA(1)==PCDATA)) {
                        match(PCDATA);
                    }
                    else {
                        break _loop98;
                    }

                } while (true);
            }
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_13);
        }
    }

    public final void dd() throws ParserException, IOException {


        try {      // for error handling
            match(ODDEF);
            {
                int _cnt101=0;
_loop101:
                do {
                    switch ( LA(1)) {
                    case PCDATA:
                    case IMG:
                    case BFONT:
                    case BR:
                    case OFORM:
                    case OTTYPE:
                    case OITALIC:
                    case OBOLD:
                    case OUNDER:
                    case OSTRIKE:
                    case OBIG:
                    case OSMALL:
                    case OSUB:
                    case OSUP:
                    case OEM:
                    case OSTRONG:
                    case ODEF:
                    case OCODE:
                    case OSAMP:
                    case OKBD:
                    case OVAR:
                    case OCITE:
                    case OANCHOR:
                    case OAPPLET:
                    case OFONT:
                    case OMAP:
                        {
                            text();
                            break;
                        }
                    case HR:
                    case OPARA:
                    case OULIST:
                    case OOLIST:
                    case ODLIST:
                    case OPRE:
                    case ODIV:
                    case OCENTER:
                    case OBQUOTE:
                    case OTABLE:
                        {
                            block();
                            break;
                        }
                    default:
                        {
                            if ( _cnt101>=1 ) { break _loop101; } else {throw new NoViableAltException(LT(1));}
                        }
                    }
                    _cnt101++;
                } while (true);
            }
            match(CDTERM);
            {
_loop103:
                do {
                    if ((LA(1)==PCDATA)) {
                        match(PCDATA);
                    }
                    else {
                        break _loop103;
                    }

                } while (true);
            }
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_13);
        }
    }

    public final void dir() throws ParserException, IOException {


        try {      // for error handling
            match(ODIR);
            {
                int _cnt106=0;
_loop106:
                do {
                    if ((LA(1)==OLITEM)) {
                        list_item();
                    }
                    else {
                        if ( _cnt106>=1 ) { break _loop106; } else {throw new NoViableAltException(LT(1));}
                    }

                    _cnt106++;
                } while (true);
            }
            match(CDIR);
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_0);
        }
    }

    public final void menu() throws ParserException, IOException {


        try {      // for error handling
            match(OMENU);
            {
                int _cnt109=0;
_loop109:
                do {
                    if ((LA(1)==OLITEM)) {
                        list_item();
                    }
                    else {
                        if ( _cnt109>=1 ) { break _loop109; } else {throw new NoViableAltException(LT(1));}
                    }

                    _cnt109++;
                } while (true);
            }
            match(CMENU);
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_0);
        }
    }

    public final void form_field() throws ParserException, IOException {


        try {      // for error handling
            switch ( LA(1)) {
            case INPUT:
                {
                    match(INPUT);
                    break;
                }
            case OSELECT:
                {
                    select();
                    break;
                }
            case OTAREA:
                {
                    textarea();
                    break;
                }
            default:
                {
                    throw new NoViableAltException(LT(1));
                }
            }
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_14);
        }
    }

    public final void caption() throws ParserException, IOException {


        try {      // for error handling
            match(OCAP);
            {
_loop131:
                do {
                    if ((_tokenSet_10.member(LA(1)))) {
                        text();
                    }
                    else {
                        break _loop131;
                    }

                } while (true);
            }
            match(CCAP);
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_15);
        }
    }

    public final void tr() throws ParserException, IOException {


        try {      // for error handling
            match(O_TR);
            {
_loop134:
                do {
                    if ((LA(1)==PCDATA)) {
                        match(PCDATA);
                    }
                    else {
                        break _loop134;
                    }

                } while (true);
            }
            {
_loop136:
                do {
                    if ((LA(1)==O_TH_OR_TD)) {
                        th_or_td();
                    }
                    else {
                        break _loop136;
                    }

                } while (true);
            }
            {
                switch ( LA(1)) {
                case C_TR:
                    {
                        match(C_TR);
                        {
_loop139:
                            do {
                                if ((LA(1)==PCDATA)) {
                                    match(PCDATA);
                                }
                                else {
                                    break _loop139;
                                }

                            } while (true);
                        }
                        break;
                    }
                case CTABLE:
                case O_TR:
                    {
                        break;
                    }
                default:
                    {
                        throw new NoViableAltException(LT(1));
                    }
                }
            }
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_16);
        }
    }

    public final void th_or_td() throws ParserException, IOException {


        try {      // for error handling
            match(O_TH_OR_TD);
            {
_loop142:
                do {
                    if ((_tokenSet_2.member(LA(1)))) {
                        body_content();
                    }
                    else {
                        break _loop142;
                    }

                } while (true);
            }
            {
                switch ( LA(1)) {
                case C_TH_OR_TD:
                    {
                        match(C_TH_OR_TD);
                        {
_loop145:
                            do {
                                if ((LA(1)==PCDATA)) {
                                    match(PCDATA);
                                }
                                else {
                                    break _loop145;
                                }

                            } while (true);
                        }
                        break;
                    }
                case CTABLE:
                case O_TR:
                case C_TR:
                case O_TH_OR_TD:
                    {
                        break;
                    }
                default:
                    {
                        throw new NoViableAltException(LT(1));
                    }
                }
            }
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_17);
        }
    }

    public final void select() throws ParserException, IOException {


        try {      // for error handling
            match(OSELECT);
            {
_loop200:
                do {
                    if ((LA(1)==PCDATA)) {
                        match(PCDATA);
                    }
                    else {
                        break _loop200;
                    }

                } while (true);
            }
            {
                int _cnt202=0;
_loop202:
                do {
                    if ((LA(1)==SELOPT)) {
                        select_option();
                    }
                    else {
                        if ( _cnt202>=1 ) { break _loop202; } else {throw new NoViableAltException(LT(1));}
                    }

                    _cnt202++;
                } while (true);
            }
            match(CSELECT);
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_14);
        }
    }

    public final void textarea() throws ParserException, IOException {


        try {      // for error handling
            match(OTAREA);
            {
_loop208:
                do {
                    if ((LA(1)==PCDATA)) {
                        match(PCDATA);
                    }
                    else {
                        break _loop208;
                    }

                } while (true);
            }
            match(CTAREA);
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_14);
        }
    }

    public final void select_option() throws ParserException, IOException {


        try {      // for error handling
            match(SELOPT);
            {
_loop205:
                do {
                    if ((LA(1)==PCDATA)) {
                        match(PCDATA);
                    }
                    else {
                        break _loop205;
                    }

                } while (true);
            }
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_18);
        }
    }


    public static final String[] _tokenNames = {
        "<0>",
        "EOF",
        "<2>",
        "NULL_TREE_LOOKAHEAD",
        "PCDATA",
        "DOCTYPE",
        "OHTML",
        "CHTML",
        "OHEAD",
        "CHEAD",
        "ISINDEX",
        "BASE",
        "META",
        "LINK",
        "OTITLE",
        "CTITLE",
        "OSCRIPT",
        "CSCRIPT",
        "OSTYLE",
        "CSTYLE",
        "OBODY",
        "CBODY",
        "ADDRESS",
        "HR",
        "IMG",
        "BFONT",
        "BR",
        "OH1",
        "CH1",
        "OH2",
        "CH2",
        "OH3",
        "CH3",
        "OH4",
        "CH4",
        "OH5",
        "CH5",
        "OH6",
        "CH6",
        "OADDRESS",
        "CADDRESS",
        "OPARA",
        "CPARA",
        "OULIST",
        "CULIST",
        "OOLIST",
        "COLIST",
        "ODLIST",
        "CDLIST",
        "OLITEM",
        "CLITEM",
        "ODTERM",
        "CDTERM",
        "ODDEF",
        "ODIR",
        "CDIR",
        "OMENU",
        "CMENU",
        "OPRE",
        "CPRE",
        "ODIV",
        "CDIV",
        "OCENTER",
        "CCENTER",
        "OBQUOTE",
        "CBQUOTE",
        "OFORM",
        "CFORM",
        "OTABLE",
        "CTABLE",
        "OCAP",
        "CCAP",
        "O_TR",
        "C_TR",
        "O_TH_OR_TD",
        "C_TH_OR_TD",
        "OTTYPE",
        "CTTYPE",
        "OITALIC",
        "CITALIC",
        "OBOLD",
        "CBOLD",
        "OUNDER",
        "CUNDER",
        "OSTRIKE",
        "CSTRIKE",
        "OBIG",
        "CBIG",
        "OSMALL",
        "CSMALL",
        "OSUB",
        "CSUB",
        "OSUP",
        "CSUP",
        "OEM",
        "CEM",
        "OSTRONG",
        "CSTRONG",
        "ODEF",
        "CDEF",
        "OCODE",
        "CCODE",
        "OSAMP",
        "CSAMP",
        "OKBD",
        "CKBD",
        "OVAR",
        "CVAR",
        "OCITE",
        "CCITE",
        "INPUT",
        "OSELECT",
        "CSELECT",
        "SELOPT",
        "OTAREA",
        "CTAREA",
        "OANCHOR",
        "CANCHOR",
        "OAPPLET",
        "APARAM",
        "CAPPLET",
        "OFONT",
        "CFONT",
        "OMAP",
        "AREA",
        "CMAP",
        "CDDEF",
        "CDIR_OR_CDIV",
        "OSTRIKE_OR_OSTRONG",
        "CST_LEFT_FACTORED",
        "CSUB_OR_CSUP",
        "ODFN",
        "CDFN",
        "APPLET",
        "APARM",
        "CFORM_OR_CFONT",
        "BFONT_OR_BASE",
        "COMMENT_DATA",
        "COMMENT",
        "WS",
        "ATTR",
        "WORD",
        "STRING",
        "WSCHARS",
        "SPECIAL",
        "HEXNUM",
        "INT",
        "HEXINT",
        "DIGIT",
        "HEXDIGIT",
        "LCLETTER",
        "UNDEFINED_TOKEN"
    };

    private static final long _tokenSet_0_data_[] = { 2L, 0L, 0L };
    public static final BitSet _tokenSet_0 = new BitSet(_tokenSet_0_data_);
    private static final long _tokenSet_1_data_[] = { 6053024999500939394L, 743117394764189717L, 0L, 0L };
    public static final BitSet _tokenSet_1 = new BitSet(_tokenSet_1_data_);
    private static final long _tokenSet_2_data_[] = { 6053024999499890704L, 743117394764189717L, 0L, 0L };
    public static final BitSet _tokenSet_2 = new BitSet(_tokenSet_2_data_);
    private static final long _tokenSet_3_data_[] = { 130L, 0L, 0L };
    public static final BitSet _tokenSet_3 = new BitSet(_tokenSet_3_data_);
    private static final long _tokenSet_4_data_[] = { 6053024999501299346L, 743117394764189717L, 0L, 0L };
    public static final BitSet _tokenSet_4 = new BitSet(_tokenSet_4_data_);
    private static final long _tokenSet_5_data_[] = { -131088L, -1L, 16777215L, 0L, 0L, 0L };
    public static final BitSet _tokenSet_5 = new BitSet(_tokenSet_5_data_);
    private static final long _tokenSet_6_data_[] = { -524304L, -1L, 16777215L, 0L, 0L, 0L };
    public static final BitSet _tokenSet_6 = new BitSet(_tokenSet_6_data_);
    private static final long _tokenSet_7_data_[] = { -864504028139093870L, 744454400903569213L, 0L, 0L };
    public static final BitSet _tokenSet_7 = new BitSet(_tokenSet_7_data_);
    private static final long _tokenSet_8_data_[] = { -101614115862085486L, 1041738888806137789L, 0L, 0L };
    public static final BitSet _tokenSet_8 = new BitSet(_tokenSet_8_data_);
    private static final long _tokenSet_9_data_[] = { -860000062097325934L, 744454400903569213L, 0L, 0L };
    public static final BitSet _tokenSet_9 = new BitSet(_tokenSet_9_data_);
    private static final long _tokenSet_10_data_[] = { 117440528L, 743117394764189700L, 0L, 0L };
    public static final BitSet _tokenSet_10 = new BitSet(_tokenSet_10_data_);
    private static final long _tokenSet_11_data_[] = { -678079266212020078L, 744454400903569213L, 0L, 0L };
    public static final BitSet _tokenSet_11 = new BitSet(_tokenSet_11_data_);
    private static final long _tokenSet_12_data_[] = { 180794895978463232L, 0L, 0L };
    public static final BitSet _tokenSet_12 = new BitSet(_tokenSet_12_data_);
    private static final long _tokenSet_13_data_[] = { 11540474045136896L, 0L, 0L };
    public static final BitSet _tokenSet_13 = new BitSet(_tokenSet_13_data_);
    private static final long _tokenSet_14_data_[] = { 6053024999499890704L, 744454400903565341L, 0L, 0L };
    public static final BitSet _tokenSet_14 = new BitSet(_tokenSet_14_data_);
    private static final long _tokenSet_15_data_[] = { 16L, 256L, 0L, 0L };
    public static final BitSet _tokenSet_15 = new BitSet(_tokenSet_15_data_);
    private static final long _tokenSet_16_data_[] = { 0L, 288L, 0L, 0L };
    public static final BitSet _tokenSet_16 = new BitSet(_tokenSet_16_data_);
    private static final long _tokenSet_17_data_[] = { 0L, 1824L, 0L, 0L };
    public static final BitSet _tokenSet_17 = new BitSet(_tokenSet_17_data_);
    private static final long _tokenSet_18_data_[] = { 0L, 844424930131968L, 0L, 0L };
    public static final BitSet _tokenSet_18 = new BitSet(_tokenSet_18_data_);

}
