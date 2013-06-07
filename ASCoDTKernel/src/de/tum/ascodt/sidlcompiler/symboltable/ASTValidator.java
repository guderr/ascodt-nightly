package de.tum.ascodt.sidlcompiler.symboltable;

import java.util.Stack;

import de.tum.ascodt.plugin.utils.tracing.Trace;
import de.tum.ascodt.repository.Target;
import de.tum.ascodt.sidlcompiler.astproperties.ASTEquals;
import de.tum.ascodt.sidlcompiler.frontend.analysis.DepthFirstAdapter;
import de.tum.ascodt.sidlcompiler.frontend.node.AClassPackageElement;
import de.tum.ascodt.sidlcompiler.frontend.node.AInterfacePackageElement;
import de.tum.ascodt.sidlcompiler.frontend.node.AOperation;
import de.tum.ascodt.sidlcompiler.frontend.node.APackage;
import de.tum.ascodt.sidlcompiler.frontend.node.AParameterArrayInEnumParameter;
import de.tum.ascodt.sidlcompiler.frontend.node.AParameterArrayInParameter;
import de.tum.ascodt.sidlcompiler.frontend.node.AParameterArrayOutEnumParameter;
import de.tum.ascodt.sidlcompiler.frontend.node.AParameterArrayOutParameter;
import de.tum.ascodt.sidlcompiler.frontend.node.AParameterInEnumParameter;
import de.tum.ascodt.sidlcompiler.frontend.node.AParameterInParameter;
import de.tum.ascodt.sidlcompiler.frontend.node.AParameterOutEnumParameter;
import de.tum.ascodt.sidlcompiler.frontend.node.AParameterOutParameter;
import de.tum.ascodt.sidlcompiler.frontend.node.AUserDefinedType;
import de.tum.ascodt.sidlcompiler.frontend.node.Node;
import de.tum.ascodt.sidlcompiler.frontend.node.PParameter;

/**
 * AST Validator
 *
 * The AST validator runs over the ast and checks whether it is valid. This 
 * means: It looks for correct variables and targets, and it validates that 
 * symbols loaded several times always are exactly the same. For the latter, 
 * it is important to the validator to have the symbol table at hand. This 
 * way, the class somehow belongs to the symbol table. 
 * 
 * @author Tobias Weinzierl
 */
public class ASTValidator extends DepthFirstAdapter {
	static Trace _trace = new Trace( "de.tum.ascodt.sidlcompiler.symboltable.ASTValidator");

	private String   _currentSymbol;

	private String   _errorString;

	private SymbolTable  _symbolTable;
	public Stack<String> _stack;
	private String _resourceLocation;
	
	public ASTValidator(SymbolTable symbolTable, String resourceLocation) {
		_errorString = "";
		_symbolTable = symbolTable;
		_stack=new Stack<String>();
		_resourceLocation=resourceLocation;
	}


	public boolean isValid() {
		return _errorString.equals("");
	}


	public String getErrorMessages() {
		return _errorString;
	}



	/**
	 * Enter a class decription
	 *  
	 * - Remember symbol name for error messages (if there are some).
	 * - Check whether target does exist.
	 * - Check whether the symbol is equal to other reads if symbol is parsed several times.
	 * - Check that all extended interfaces are known.
	 * 
	 * The latter is not validated here but in the inAUserDefinedType().
	 * 
	 * The builder already has ensured that there's not both a class and an 
	 * interface having the same name. 
	 * 
	 */
	public void inAClassPackageElement(AClassPackageElement node) {
		_currentSymbol = Scope.getSymbol(node);
		_trace.in( "inAClassPackageElement(...)", _currentSymbol );

		if (node.getTarget()!=null) {
			String target = node.getTarget().getText();

			if (!Target.isValidTarget(target)) {
				addErrorMessage( "target " + target + " does not exist" );
			} 
		}
		if(_symbolTable.getScope(node)!=null){

			AClassPackageElement classFromSymbolTable = _symbolTable.getScope(node).getClassDefinition(_currentSymbol);

			ASTEquals compare = new ASTEquals();
			node.apply(compare);
			compare.switchToCompareMode();
			classFromSymbolTable.apply(compare);
			if (!compare.ASTsAreEqual()) {
				addErrorMessage( "ASTs for " + _currentSymbol + " are not equal in different definition files: " + compare.getDifferenceDescription() );
			}
		}
		_trace.out( "inAClassPackageElement(...)", _currentSymbol );
	}


	/**
	 * Enter an interface decription 
	 * 
	 * - Remember symbol name for error messages (if there are some).
	 * - Check whether the symbol is equal to other reads if symbol is parsed several times.
	 * - Check that all extended interfaces are known.
	 * 
	 * The latter is not validated here but in the inAUserDefinedType().
	 * 
	 * The builder already has ensured that there's not both a class and an 
	 * interface having the same name. 
	 */
	public void inAInterfacePackageElement(AInterfacePackageElement node) {
		_currentSymbol = Scope.getSymbol(node);
		_trace.in( "inAInterfacePackageElement(...)", _currentSymbol );
		if(_symbolTable.getScope(node)!=null){

			AInterfacePackageElement interfaceFromSymbolTable = _symbolTable.getScope(node).getInterfaceDefinition(_currentSymbol);

			ASTEquals compare = new ASTEquals();
			node.apply(compare);
			compare.switchToCompareMode();
			interfaceFromSymbolTable.apply(compare);
			if (!compare.ASTsAreEqual()) {
				addErrorMessage( "ASTs for " + _currentSymbol + " are not equal in different definition files: " + compare.getDifferenceDescription() );
			}
		}
		_trace.out( "inAInterfacePackageElement(...)", _currentSymbol );
	}


	public void inAUserDefinedType(AUserDefinedType node) {
		String fullQualifiedSymbol = Scope.getSymbol(node);
		if(_symbolTable.getScope(node)==null)
			return;
		if (!_symbolTable.getScope(node).containsSymbol(fullQualifiedSymbol)) {
			addErrorMessage( "symbol " + fullQualifiedSymbol + " within symbol " + _currentSymbol + " is unknown" );
		}
	}


	/**
	 * PParameter is an abstract supertype of in and out parameters. This 
	 * operation returns its type. 
	 *  
	 * @param parameter
	 * @return
	 */
	private String getParameterName(PParameter parameter) {
		if ( parameter instanceof AParameterInParameter) {
			return ((AParameterInParameter)parameter).getName().getText();
		}
		if ( parameter instanceof AParameterArrayInParameter) {
			return ((AParameterArrayInParameter)parameter).getName().getText();
		}
		if ( parameter instanceof AParameterOutParameter) {
			return ((AParameterOutParameter)parameter).getName().getText();
		}
		if ( parameter instanceof AParameterArrayOutParameter) {
			return ((AParameterArrayOutParameter)parameter).getName().getText();
		}
		if ( parameter instanceof AParameterInEnumParameter)
			return ((AParameterInEnumParameter)parameter).getName().getText();

		if ( parameter instanceof AParameterOutEnumParameter)
			return ((AParameterOutEnumParameter)parameter).getName().getText();

		if ( parameter instanceof AParameterArrayInEnumParameter)
			return ((AParameterArrayInEnumParameter)parameter).getName().getText();

		if ( parameter instanceof AParameterArrayOutEnumParameter)
			return ((AParameterArrayOutEnumParameter)parameter).getName().getText();
		return "<error>";
	}


	private void addErrorMessage( String message ) {
		_errorString += "error in symbol " + _currentSymbol + ": " + message + "\n";
	}


	/**
	 * Enter an operation description
	 * 
	 * Check whether the different argument names do differ.
	 */
	public void inAOperation(AOperation node) {
		_trace.in( "inAOperation(...)");

		for (PParameter parameter: node.getParameter()) {
			String parameterName = getParameterName(parameter);
			_trace.debug( "inAOperation(...)", "check parameter " + parameterName );
			for (PParameter otherParameters: node.getParameter()) {
				String otherParameterName = getParameterName(otherParameters);
				if (parameter != otherParameters && parameterName.equals(otherParameterName)) {
					addErrorMessage( "two arguments with the same name " + parameterName + " in operation " + node.getName().getText() );
				}
			}
		}

		_trace.out( "inAOperation(...)");
	}

}
