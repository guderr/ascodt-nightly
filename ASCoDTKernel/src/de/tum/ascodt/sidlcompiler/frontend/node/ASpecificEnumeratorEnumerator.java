/* This file was generated by SableCC (http://www.sablecc.org/). */

package de.tum.ascodt.sidlcompiler.frontend.node;

import de.tum.ascodt.sidlcompiler.frontend.analysis.*;

@SuppressWarnings("nls")
public final class ASpecificEnumeratorEnumerator extends PEnumerator
{
    private TIdentifier _name_;
    private TConstant _value_;

    public ASpecificEnumeratorEnumerator()
    {
        // Constructor
    }

    public ASpecificEnumeratorEnumerator(
        @SuppressWarnings("hiding") TIdentifier _name_,
        @SuppressWarnings("hiding") TConstant _value_)
    {
        // Constructor
        setName(_name_);

        setValue(_value_);

    }

    @Override
    public Object clone()
    {
        return new ASpecificEnumeratorEnumerator(
            cloneNode(this._name_),
            cloneNode(this._value_));
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseASpecificEnumeratorEnumerator(this);
    }

    public TIdentifier getName()
    {
        return this._name_;
    }

    public void setName(TIdentifier node)
    {
        if(this._name_ != null)
        {
            this._name_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._name_ = node;
    }

    public TConstant getValue()
    {
        return this._value_;
    }

    public void setValue(TConstant node)
    {
        if(this._value_ != null)
        {
            this._value_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._value_ = node;
    }

    @Override
    public String toString()
    {
        return ""
            + toString(this._name_)
            + toString(this._value_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._name_ == child)
        {
            this._name_ = null;
            return;
        }

        if(this._value_ == child)
        {
            this._value_ = null;
            return;
        }

        throw new RuntimeException("Not a child.");
    }

    @Override
    void replaceChild(@SuppressWarnings("unused") Node oldChild, @SuppressWarnings("unused") Node newChild)
    {
        // Replace child
        if(this._name_ == oldChild)
        {
            setName((TIdentifier) newChild);
            return;
        }

        if(this._value_ == oldChild)
        {
            setValue((TConstant) newChild);
            return;
        }

        throw new RuntimeException("Not a child.");
    }
}
