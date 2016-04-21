package com.github.mjvesa.jscm.jscheme;

import com.github.mjvesa.jscm.jsint.E;

/**  
 * A jscheme.SchemeException is used to indicate an error in the Scheme interpreter
 * @author Timothy J. Hickey, Copyright 2000, tim@cs.brandeis.edu, <a href="license.txt">license</a>
 * subsequently modified by Jscheme project members
 * licensed under zlib licence (see license.txt)
 */


public class SchemeException extends RuntimeException {
    public Object contents;

    public SchemeException(Object contents) {
	this.contents = contents;
    }

    public SchemeException(String message, Object contents) {
        super(message);
	this.contents = contents;
    }

  public String toString() {
    return "SchemeException: "+
      REPL.printToString(this.getMessage(),false) +
      ((contents == null) ? "" :
       ", " + E.shortStringify(REPL.printToString(contents,false)));
  }
}
