/* This file was generated by SableCC (http://www.sablecc.org/). */

package de.tum.ascodt.sidlcompiler.frontend.node;

import de.tum.ascodt.sidlcompiler.frontend.analysis.*;

@SuppressWarnings("nls")
public final class TSignedDecimalNumber extends Token
{
    public TSignedDecimalNumber(String text)
    {
        setText(text);
    }

    public TSignedDecimalNumber(String text, int line, int pos)
    {
        setText(text);
        setLine(line);
        setPos(pos);
    }

    @Override
    public Object clone()
    {
      return new TSignedDecimalNumber(getText(), getLine(), getPos());
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseTSignedDecimalNumber(this);
    }
}
