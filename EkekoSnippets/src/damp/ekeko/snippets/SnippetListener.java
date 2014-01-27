// Generated from ./src/damp/ekeko/snippets/Snippet.g4 by ANTLR 4.1
package damp.ekeko.snippets;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link SnippetParser}.
 */
public interface SnippetListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link SnippetParser#postmeta}.
	 * @param ctx the parse tree
	 */
	void enterPostmeta(@NotNull SnippetParser.PostmetaContext ctx);
	/**
	 * Exit a parse tree produced by {@link SnippetParser#postmeta}.
	 * @param ctx the parse tree
	 */
	void exitPostmeta(@NotNull SnippetParser.PostmetaContext ctx);

	/**
	 * Enter a parse tree produced by {@link SnippetParser#directives}.
	 * @param ctx the parse tree
	 */
	void enterDirectives(@NotNull SnippetParser.DirectivesContext ctx);
	/**
	 * Exit a parse tree produced by {@link SnippetParser#directives}.
	 * @param ctx the parse tree
	 */
	void exitDirectives(@NotNull SnippetParser.DirectivesContext ctx);

	/**
	 * Enter a parse tree produced by {@link SnippetParser#premeta}.
	 * @param ctx the parse tree
	 */
	void enterPremeta(@NotNull SnippetParser.PremetaContext ctx);
	/**
	 * Exit a parse tree produced by {@link SnippetParser#premeta}.
	 * @param ctx the parse tree
	 */
	void exitPremeta(@NotNull SnippetParser.PremetaContext ctx);

	/**
	 * Enter a parse tree produced by {@link SnippetParser#snippet}.
	 * @param ctx the parse tree
	 */
	void enterSnippet(@NotNull SnippetParser.SnippetContext ctx);
	/**
	 * Exit a parse tree produced by {@link SnippetParser#snippet}.
	 * @param ctx the parse tree
	 */
	void exitSnippet(@NotNull SnippetParser.SnippetContext ctx);

	/**
	 * Enter a parse tree produced by {@link SnippetParser#meta}.
	 * @param ctx the parse tree
	 */
	void enterMeta(@NotNull SnippetParser.MetaContext ctx);
	/**
	 * Exit a parse tree produced by {@link SnippetParser#meta}.
	 * @param ctx the parse tree
	 */
	void exitMeta(@NotNull SnippetParser.MetaContext ctx);
}