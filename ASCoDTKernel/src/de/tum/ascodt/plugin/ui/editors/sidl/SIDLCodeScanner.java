package de.tum.ascodt.plugin.ui.editors.sidl;


/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/



import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;

import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;


/**
 * This class provides the functionality to scan a sidl file. It searches for 
 * key words and applies coloring schemes for them.
 * @author atanasoa
 *
 */
public class SIDLCodeScanner extends RuleBasedScanner {

	
	private static String[] fgKeywords= {"class","extends","implements-all","package","interface","uses","in","inout","as"}; //$NON-NLS-36$ //$NON-NLS-35$ //$NON-NLS-34$ //$NON-NLS-33$ //$NON-NLS-32$ //$NON-NLS-31$ //$NON-NLS-30$ //$NON-NLS-29$ //$NON-NLS-28$ //$NON-NLS-27$ //$NON-NLS-26$ //$NON-NLS-25$ //$NON-NLS-24$ //$NON-NLS-23$ //$NON-NLS-22$ //$NON-NLS-21$ //$NON-NLS-20$ //$NON-NLS-19$ //$NON-NLS-18$ //$NON-NLS-17$ //$NON-NLS-16$ //$NON-NLS-15$ //$NON-NLS-14$ //$NON-NLS-13$ //$NON-NLS-12$ //$NON-NLS-11$ //$NON-NLS-10$ //$NON-NLS-9$ //$NON-NLS-8$ //$NON-NLS-7$ //$NON-NLS-6$ //$NON-NLS-5$ //$NON-NLS-4$ //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$

	private static String[] fgTypes= {"bool", "int", "float", "double","string","bool[]","int[]","double[]","string[]"}; //$NON-NLS-1$ //$NON-NLS-5$ //$NON-NLS-7$ //$NON-NLS-6$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-2$

	private static String[] fgConstants= { "false", "null", "true" }; //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$

	/**
	 * Creates a Java code scanner with the given color provider.
	 *
	 * @param provider the color provider
	 */
	public SIDLCodeScanner(SIDLColorProvider provider) {
		
		IToken keyword= new Token(new TextAttribute(provider.getColor(SIDLColorProvider.KEYWORD)));
		IToken type= new Token(new TextAttribute(provider.getColor(SIDLColorProvider.TYPE)));
		IToken string= new Token(new TextAttribute(provider.getColor(SIDLColorProvider.STRING)));
		IToken comment= new Token(new TextAttribute(provider.getColor(SIDLColorProvider.SINGLE_LINE_COMMENT)));
		IToken other= new Token(new TextAttribute(provider.getColor(SIDLColorProvider.DEFAULT)));

		List<IRule> rules= new ArrayList<IRule>();

		// Add rule for single line comments.
		rules.add(new EndOfLineRule("//", comment)); //$NON-NLS-1$

		// Add rule for strings and character constants.
		rules.add(new SingleLineRule("\"", "\"", string, '\\')); //$NON-NLS-2$ //$NON-NLS-1$
		rules.add(new SingleLineRule("'", "'", string, '\\')); //$NON-NLS-2$ //$NON-NLS-1$

		// Add generic whitespace rule.
		rules.add(new WhitespaceRule(new SIDLWhiteSpaceDetector()));

		// Add word rule for keywords, types, and constants.
		WordRule wordRule= new WordRule(new SIDLWordDetector(), other);
		for (int i= 0; i < fgKeywords.length; i++)
			wordRule.addWord(fgKeywords[i], keyword);
		for (int i= 0; i < fgTypes.length; i++)
			wordRule.addWord(fgTypes[i], type);
		for (int i= 0; i < fgConstants.length; i++)
			wordRule.addWord(fgConstants[i], type);
		rules.add(wordRule);

		IRule[] result= new IRule[rules.size()];
		rules.toArray(result);
		setRules(result);
		
	}
}
