package com.github.mjvesa.jscm.jscheme;

import com.github.mjvesa.jscm.jsint.Closure;
import com.github.mjvesa.jscm.jsint.Continuation;
import com.github.mjvesa.jscm.jsint.ContinuationException;
import com.github.mjvesa.jscm.jsint.Evaluator;
import com.github.mjvesa.jscm.jsint.Generic;
import com.github.mjvesa.jscm.jsint.*;

/**
 * This class implements the three "hard" primitives in Scheme:
 * <ul>
 * <li>REPL.readStream(INPUT) -- this returns a enumerator of all scheme terms
 * in the InputStream or Reader.
 * 
 * <p>
 * <li>REPL.eval(X) -- this evaluates the object X as a Scheme term</li>
 * <p>
 * 
 * <li>REPL.printToString(X, Quoted) -- this generates a string representation
 * of the Object X to a Scheme term If Quoted is true, and the term X involves
 * only Scheme literals, lists, and vectors. Then it will be printed is such a
 * way that reading it back in using REPL.readStream produces a term which is
 * "equal?" to X. If Quoted is false, then it will be printed in a
 * "more readable" format.</li>
 * <p>
 * 
 * </li>
 * </ul>
 * and it also provides a few useful methods constructed from these
 * <ul>
 * <li>REPL.load(NAME) -- load a Scheme program from a file,resource,or stream
 * </li>
 * <li>REPL.parseScheme(STRING) -- parse STRING into a list of Scheme
 * expressions</li>
 * <li>REPL.readEvalPrintLoop() -- starts a read/eval/print loop</li>
 * </ul>
 * The main method of REPL processes the command line arguments as follows:
 * <ul>
 * <li>strings are viewed filenames and are loaded into the interpreter</li>
 * <li>single quoted strings: ('....') are viewed as expressions are evaluated
 * </li>
 * <li>the tag "-main" is followed by n+1 strings p a1 ... an which are used to
 * construct a term (p a1 ... an) which is then evaluated</li>
 * </ul>
 * <p>
 * REPL provides factory methods for creating jscheme.SchemeSymbol and
 * jscheme.SchemePair objects
 * <ul>
 * <li>REPL.internSchemeSymbol(string) -- intern string as a Symbol</li>
 * 
 * <li>REPL.makeSchemePair(first, rest) -- creates a SchemePair</li>
 * <li>REPL.EMPTY_PAIR -- the empty SchemePair object</li>
 * </ul>
 * <p>
 * and it provides support for Exception handling and multi-threading
 * <ul>
 * <li>REPL.tryCatch(Thunk,Catch) -- return a value by calling the Thunk, if an
 * error occurs, call Catch on the error to get a value</li>
 * <li>REPL.tryCatchFinally(Expr,Catch,Finally) -- same as tryCatch, but in
 * either case call the Finally thunk before returning the value</li>
 * <li>REPL.throwRuntimeException(Exc)</li>
 * <li>REPL.synchronize(Obj,Proc)</li>
 * </ul>
 **/

public class REPL {

	/*
	 * KRA 23APR02: Mention these classes so Jscheme can compile itself by first
	 * compiling REPL.
	 */
	static {
		Object bootstrap = new Object[] { JS.class, SchemeException.class, Closure.class, Continuation.class,
				ContinuationException.class, Evaluator.class, Generic.class, Invoke.class, JavaConstructor.class,
				JavaField.class, JavaListener.class, JavaMethod.class, Macro.class, Op.class, Primitive.class, 
				SingleImporter.class, WildImporter.class, };
	}

	public static java.util.Enumeration readStream(java.io.InputStream in) {
		return new InputPort(in);
	}

	public static java.util.Enumeration readStream(java.io.Reader in) {
		return new InputPort(in);
	}

	// public static Object eval(String X) {
	// return jsint.Scheme.eval(parseScheme(X).first());
	// }

	public static Object eval(Object X) {
		return Scheme.eval(X);
	}

	public static String printToString(Object X, boolean Quoted) {
		return U.stringify(X, Quoted);
	}

	public static Object load(Object Name) {
		return Scheme.load(Name);
	}

	public static SchemePair parseScheme(java.lang.String S) {
		java.util.Enumeration E = readStream(new java.io.StringReader(S));
		Pair Tmp, P;

		if (!E.hasMoreElements())
			return Pair.EMPTY;

		P = new Pair(E.nextElement(), Pair.EMPTY);
		Tmp = P;

		while (E.hasMoreElements()) {
			Tmp.rest = new Pair(E.nextElement(), Pair.EMPTY);
			Tmp = (Pair) Tmp.rest;
		}
		return P;
	}

	public static void readEvalPrintLoop() {
		Scheme.readEvalWriteLoop(">");
	}

	public static void main(String[] args) {
		Scheme.main(args);
	}

	public static synchronized SchemeSymbol internSchemeSymbol(String name) {
		return Symbol.intern(name);
	}

	public static SchemePair makeSchemePair(Object first, Object rest) {
		return new Pair(first, rest);
	}

	public static SchemePair EMPTY_PAIR = Pair.EMPTY;

	/** provide scheme access to the "try/catch" expression of Java */
	public static Object tryCatch(Object E, Object F) {
		try {
			return ((SchemeProcedure) E).apply(Pair.EMPTY);
		} catch (Throwable e) {
			return ((SchemeProcedure) F).apply(new Pair(stripExceptionWrapper(e), Pair.EMPTY));
		}
	}

	/*
	 * this strips off the wrappers so that the tryCatch receives the underlying
	 * exception
	 */
	private static Object stripExceptionWrapper(Object e) {
		if (e instanceof BacktraceException)
			return stripExceptionWrapper(((BacktraceException) e).getBaseException());
		else if (e instanceof JschemeThrowable)
			return stripExceptionWrapper(((JschemeThrowable) e).contents);
		else
			return e;
	}

	/** Provide scheme access to finally - unwind-protect. **/
	public static Object tryCatchFinally(Object e, Object f, Object g) {
		try {
			return ((Procedure) e).apply(Pair.EMPTY);
		} catch (Throwable exc) {
			return ((Procedure) f).apply(new Pair(stripExceptionWrapper(exc), Pair.EMPTY));
		} finally {
			((Procedure) g).apply(Pair.EMPTY);
		}
	}

	/** provide scheme access to the exception throwing */
	public static Object throwRuntimeException(RuntimeException E) throws RuntimeException {
		throw (E);
	}

	public static Object synchronize(Object x, SchemeProcedure p) {
		synchronized (x) {
			return p.apply(new Pair(x, Pair.EMPTY));
		}
	}

}
