/* This file was generated by SableCC (http://www.sablecc.org/). */

package de.tum.ascodt.sidlcompiler.frontend.parser;

import de.tum.ascodt.sidlcompiler.frontend.node.*;
import de.tum.ascodt.sidlcompiler.frontend.analysis.*;

class TokenIndex extends AnalysisAdapter
{
    int index;

    @Override
    public void caseTLAngleBracket(@SuppressWarnings("unused") TLAngleBracket node)
    {
        this.index = 0;
    }

    @Override
    public void caseTRAngleBracket(@SuppressWarnings("unused") TRAngleBracket node)
    {
        this.index = 1;
    }

    @Override
    public void caseTComma(@SuppressWarnings("unused") TComma node)
    {
        this.index = 2;
    }

    @Override
    public void caseTDot(@SuppressWarnings("unused") TDot node)
    {
        this.index = 3;
    }

    @Override
    public void caseTSemicolon(@SuppressWarnings("unused") TSemicolon node)
    {
        this.index = 4;
    }

    @Override
    public void caseTLBrace(@SuppressWarnings("unused") TLBrace node)
    {
        this.index = 5;
    }

    @Override
    public void caseTRBrace(@SuppressWarnings("unused") TRBrace node)
    {
        this.index = 6;
    }

    @Override
    public void caseTLBracket(@SuppressWarnings("unused") TLBracket node)
    {
        this.index = 7;
    }

    @Override
    public void caseTRBracket(@SuppressWarnings("unused") TRBracket node)
    {
        this.index = 8;
    }

    @Override
    public void caseTEquals(@SuppressWarnings("unused") TEquals node)
    {
        this.index = 9;
    }

    @Override
    public void caseTIntToken(@SuppressWarnings("unused") TIntToken node)
    {
        this.index = 10;
    }

    @Override
    public void caseTDoubleToken(@SuppressWarnings("unused") TDoubleToken node)
    {
        this.index = 11;
    }

    @Override
    public void caseTBoolToken(@SuppressWarnings("unused") TBoolToken node)
    {
        this.index = 12;
    }

    @Override
    public void caseTOpaqueToken(@SuppressWarnings("unused") TOpaqueToken node)
    {
        this.index = 13;
    }

    @Override
    public void caseTStringToken(@SuppressWarnings("unused") TStringToken node)
    {
        this.index = 14;
    }

    @Override
    public void caseTArrayToken(@SuppressWarnings("unused") TArrayToken node)
    {
        this.index = 15;
    }

    @Override
    public void caseTInToken(@SuppressWarnings("unused") TInToken node)
    {
        this.index = 16;
    }

    @Override
    public void caseTInoutToken(@SuppressWarnings("unused") TInoutToken node)
    {
        this.index = 17;
    }

    @Override
    public void caseTPackageToken(@SuppressWarnings("unused") TPackageToken node)
    {
        this.index = 18;
    }

    @Override
    public void caseTClassToken(@SuppressWarnings("unused") TClassToken node)
    {
        this.index = 19;
    }

    @Override
    public void caseTTargetToken(@SuppressWarnings("unused") TTargetToken node)
    {
        this.index = 20;
    }

    @Override
    public void caseTInterfaceToken(@SuppressWarnings("unused") TInterfaceToken node)
    {
        this.index = 21;
    }

    @Override
    public void caseTExtendsToken(@SuppressWarnings("unused") TExtendsToken node)
    {
        this.index = 22;
    }

    @Override
    public void caseTImplementsToken(@SuppressWarnings("unused") TImplementsToken node)
    {
        this.index = 23;
    }

    @Override
    public void caseTUsesToken(@SuppressWarnings("unused") TUsesToken node)
    {
        this.index = 24;
    }

    @Override
    public void caseTAsToken(@SuppressWarnings("unused") TAsToken node)
    {
        this.index = 25;
    }

    @Override
    public void caseTEnumToken(@SuppressWarnings("unused") TEnumToken node)
    {
        this.index = 26;
    }

    @Override
    public void caseTDecimalConstant(@SuppressWarnings("unused") TDecimalConstant node)
    {
        this.index = 27;
    }

    @Override
    public void caseTSignedDecimalNumber(@SuppressWarnings("unused") TSignedDecimalNumber node)
    {
        this.index = 28;
    }

    @Override
    public void caseTIdentifier(@SuppressWarnings("unused") TIdentifier node)
    {
        this.index = 29;
    }

    @Override
    public void caseTConstant(@SuppressWarnings("unused") TConstant node)
    {
        this.index = 30;
    }

    @Override
    public void caseEOF(@SuppressWarnings("unused") EOF node)
    {
        this.index = 31;
    }
}
