// Generated from ./src/damp/ekeko/snippets/Snippet.g4 by ANTLR 4.1
package damp.ekeko.snippets;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link SnippetParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface SnippetVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link SnippetParser#postmeta}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPostmeta(@NotNull SnippetParser.PostmetaContext ctx);

	/**
	 * Visit a parse tree produced by {@link SnippetParser#directives}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDirectives(@NotNull SnippetParser.DirectivesContext ctx);

	/**
	 * Visit a parse tree produced by {@link SnippetParser#premeta}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPremeta(@NotNull SnippetParser.PremetaContext ctx);

	/**
	 * Visit a parse tree produced by {@link SnippetParser#snippet}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSnippet(@NotNull SnippetParser.SnippetContext ctx);

	/**
	 * Visit a parse tree produced by {@link SnippetParser#meta}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMeta(@NotNull SnippetParser.MetaContext ctx);
}