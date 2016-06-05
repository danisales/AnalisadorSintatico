package visitor;

import symboltable.Class;
import symboltable.Method;
import symboltable.Variable;
import symboltable.SymbolTable;

import javax.swing.border.EtchedBorder;

import ast.And;
import ast.ArrayAssign;
import ast.ArrayLength;
import ast.ArrayLookup;
import ast.Assign;
import ast.Block;
import ast.BooleanType;
import ast.Call;
import ast.ClassDeclExtends;
import ast.ClassDeclSimple;
import ast.False;
import ast.Formal;
import ast.Identifier;
import ast.IdentifierExp;
import ast.IdentifierType;
import ast.If;
import ast.IntArrayType;
import ast.IntegerLiteral;
import ast.IntegerType;
import ast.LessThan;
import ast.MainClass;
import ast.MethodDecl;
import ast.Minus;
import ast.NewArray;
import ast.NewObject;
import ast.Not;
import ast.Plus;
import ast.Print;
import ast.Program;
import ast.This;
import ast.Times;
import ast.True;
import ast.Type;
import ast.VarDecl;
import ast.While;

public class TypeCheckVisitor implements TypeVisitor {

	class ErrorMsg {
		boolean anyErrors;
		void complain(String msg) {
			anyErrors = true;
			System.out.println(msg);
		}
	}

	private ErrorMsg error;
	private SymbolTable symbolTable;
	private Class currClass;
	private Method currMethod;

	public TypeCheckVisitor(SymbolTable st) {
		error = new ErrorMsg();
		symbolTable = st;
	}

	// MainClass m;
	// ClassDeclList cl;
	public Type visit(Program n) {
		n.m.accept(this);
		for (int i = 0; i < n.cl.size(); i++) {
			n.cl.elementAt(i).accept(this);
		}
		return null;
	}

	// Identifier i1,i2;
	// Statement s;
	public Type visit(MainClass n) {
		currClass = symbolTable.getClass(n.i1.s);
		n.i1.accept(this);
		n.i2.accept(this);
		n.s.accept(this);
		currClass = null;
		return null;
	}

	// Identifier i;
	// VarDeclList vl;
	// MethodDeclList ml;
	public Type visit(ClassDeclSimple n) {
		currClass = symbolTable.getClass(n.i.s);
		n.i.accept(this);
		for (int i = 0; i < n.vl.size(); i++) {
			n.vl.elementAt(i).accept(this);
		}
		for (int i = 0; i < n.ml.size(); i++) {
			n.ml.elementAt(i).accept(this);
		}
		currClass = null;
		return null;
	}

	// Identifier i;
	// Identifier j;
	// VarDeclList vl;
	// MethodDeclList ml;
	public Type visit(ClassDeclExtends n) {
		currClass = symbolTable.getClass(n.i.s);
		n.i.accept(this);
		n.j.accept(this);
		for (int i = 0; i < n.vl.size(); i++) {
			n.vl.elementAt(i).accept(this);
		}
		for (int i = 0; i < n.ml.size(); i++) {
			n.ml.elementAt(i).accept(this);
		}
		currClass = null;
		return null;
	}

	// Type t;
	// Identifier i;
	public Type visit(VarDecl n) {
		n.t.accept(this);
		n.i.accept(this);
		return null;
	}

	// Type t;
	// Identifier i;
	// FormalList fl;
	// VarDeclList vl;
	// StatementList sl;
	// Exp e;
	//PUBLIC type:tp ident:id LPAREN formalList:formalLs RPAREN LBRACE varList:vars stmtList:stmtLs
	//RETURN expr:expr SEMI RBRACE
	public Type visit(MethodDecl n) {
		currMethod = symbolTable.getMethod(n.i.s, currClass.getId());
		Type t = n.t.accept(this);
		n.i.accept(this);
		for (int i = 0; i < n.fl.size(); i++) {
			n.fl.elementAt(i).accept(this);
		}
		for (int i = 0; i < n.vl.size(); i++) {
			n.vl.elementAt(i).accept(this);
		}
		for (int i = 0; i < n.sl.size(); i++) {
			n.sl.elementAt(i).accept(this);
		}
		Type e = n.e.accept(this);
		if(!(symbolTable.compareTypes(t, e))){
			error.complain("Erro no tipo do retorno do método " + n.i.s);
		}
		currMethod = null;
		return null;
	}

	// Type t;
	// Identifier i;
	public Type visit(Formal n) {
		n.t.accept(this);
		n.i.accept(this);
		return null;
	}

	public Type visit(IntArrayType n) {
		return new IntArrayType();
	}

	public Type visit(BooleanType n) {
		return new BooleanType();
	}

	public Type visit(IntegerType n) {
		return new IntegerType();
	}

	// String s;
	public Type visit(IdentifierType n) {
		return new IdentifierType(n.s);
	}

	// StatementList sl;
	public Type visit(Block n) {
		for (int i = 0; i < n.sl.size(); i++) {
			n.sl.elementAt(i).accept(this);
		}
		return null;
	}

	// Exp e;
	// Statement s1,s2;
	public Type visit(If n) {
		Type e = n.e.accept(this);
		n.s1.accept(this);
		n.s2.accept(this);
		if(!(e instanceof BooleanType)){
			error.complain("Condição do if deve ser do tipo Boolean");
		}
		return null;
	}

	// Exp e;
	// Statement s;
	public Type visit(While n) {
		Type e = n.e.accept(this);
		n.s.accept(this);
		if(!(e instanceof BooleanType)){
			error.complain("Condição do while deve ser do tipo Boolean");
		}
		return null;
	}

	// Exp e;
	public Type visit(Print n) {
		n.e.accept(this);
		return null;
	}

	// Identifier i;
	// Exp e;
	//ident:id EQUAL expr:exp SEMI
	public Type visit(Assign n) {
		Type i = n.i.accept(this);
		Type e = n.e.accept(this);
		if(i == null || e == null){
			return null;
		}
		if(!(symbolTable.compareTypes(i, e))){
			error.complain(n.i.s + " deve ser do mesmo tipo de " + n.e.toString());
		}
		return null;
	}

	// Identifier i;
	// Exp e1,e2;
	// ident:id LBRACKET expr:exp1 RBRACKET EQUAL expr:exp2 SEMI
	// array[2] = 1
	public Type visit(ArrayAssign n) {
		Type i = n.i.accept(this);
		Type e1 = n.e1.accept(this);
		Type e2 = n.e2.accept(this);
		if(i == null || e1 == null || e2 == null){
			return null;
		}
		if(!(e1 instanceof IntegerType)){
			error.complain(n.i.s + " deve ser do tipo Integer");
		}
		if(!(e2 instanceof IntegerType)){
			error.complain(n.e1.toString() + " deve ser do tipo Integer");
		}
		return null;
	}

	// Exp e1,e2;
	public Type visit(And n) {
		Type e1 = n.e1.accept(this);
		Type e2 = n.e2.accept(this);
		if(e1 == null || e2 == null){
			return null;
		}
		if(!(e1 instanceof BooleanType)){
			error.complain(n.e1.toString() + " deveria ser do tipo Boolean");
			return null;
		}
		if(!(e2 instanceof BooleanType)){
			error.complain(n.e2.toString() + " deveria ser do tipo Boolean");
			return null;
		}
		return new BooleanType();
	}

	// Exp e1,e2;
	public Type visit(LessThan n) {
		Type e1 = n.e1.accept(this);
		Type e2 = n.e2.accept(this);
		if(e1 == null || e2 == null){
			return null;
		}
		if(!(e1 instanceof IntegerType)){
			error.complain(n.e1.toString() + " deveria ser do tipo Integer");
			return null;
		}
		if(!(e2 instanceof IntegerType)){
			error.complain(n.e2.toString() + " deveria ser do tipo Integer");
		}
		return new BooleanType();
	}

	// Exp e1,e2;
	public Type visit(Plus n) {
		Type e1 = n.e1.accept(this);
		Type e2 = n.e2.accept(this);
		if(e1 == null || e2 == null){
			return null;
		}
		if(!(e1 instanceof IntegerType)){
			error.complain(n.e1.toString() + " deveria ser do tipo Integer");
			return null;
		}
		if(!(e2 instanceof IntegerType)){
			error.complain(n.e2.toString() + " deveria ser do tipo Integer");
		}
		return new IntegerType();
	}

	// Exp e1,e2;
	public Type visit(Minus n) {
		Type e1 = n.e1.accept(this);
		Type e2 = n.e2.accept(this);
		if(e1 == null || e2 == null){
			return null;
		}
		if(!(e1 instanceof IntegerType)){
			error.complain(n.e1.toString() + " deveria ser do tipo Integer");
			return null;
		}
		if(!(e2 instanceof IntegerType)){
			error.complain(n.e2.toString() + " deveria ser do tipo Integer");
		}
		return new IntegerType();
	}

	// Exp e1,e2;
	public Type visit(Times n) {
		Type e1 = n.e1.accept(this);
		Type e2 = n.e2.accept(this);
		if(e1 == null || e2 == null){
			return null;
		}
		if(!(e1 instanceof IntegerType)){
			error.complain(n.e1.toString() + " deveria ser do tipo Integer");
			return null;
		}
		if(!(e2 instanceof IntegerType)){
			error.complain(n.e2.toString() + " deveria ser do tipo Integer");
		}
		return new IntegerType();
	}

	// Exp e1,e2;
	public Type visit(ArrayLookup n) {
		Type e1 = n.e1.accept(this);
		Type e2 = n.e2.accept(this);
		if(e1 == null || e2 == null){
			return null;
		}
		if(!(e1 instanceof IntArrayType)){
			error.complain(n.e1.toString() + " deveria ser do tipo IntArrayType");
			return null;
		}
		if(!(e2 instanceof IntegerType)){
			error.complain(n.e2.toString() + " deveria ser do tipo Integer");
			return null;
		}
		return new IntegerType();
	}

	// Exp e;
	public Type visit(ArrayLength n) {
		Type e = n.e.accept(this);
		if(e == null){
			return null;
		}
		if(!(e instanceof IntArrayType)){
			error.complain(n.e.toString() + " deveria ser do tipo IntArrayType");
			return null;
		}
		return new IntegerType();
	}

	// Exp e;
	// Identifier i;
	// ExpList el;
	// expr:exp DOT ident:id LPAREN exprList:expLs RPAREN
	public Type visit(Call n) {
		Type e = n.e.accept(this);
		Type id = n.i.accept(this);
		for (int i = 0; i < n.el.size(); i++) {
			n.el.elementAt(i).accept(this);
		}

		/*Type[] el = new Type[n.el.size()];
		for (int i = 0; i < n.el.size(); i++) {
			el[i] = n.el.elementAt(i).accept(this);
		}*/

		if(e == null){
			return null;
		}
		if(!(e instanceof IdentifierType)){
			error.complain(n.e.toString() + " deve ser do tipo IdentifierType");
			return null;
		}

		Class c = symbolTable.getClass(((IdentifierType) e).s);
		String metName = n.i.s;
		Method m = symbolTable.getMethod(metName, c.getId());

		int sizeM = 0;
		while(m.getParamAt(sizeM) != null){
			sizeM += 1;
		}

		//Verifica se a classe c possui método m
		if(!c.containsMethod(m.getId())){
			error.complain("Classe " + c.getId() + " não contém método " + m.getId());
			return null;
		}
		//Verifica número de parâmetros
		if(sizeM != n.el.size()){
			error.complain("Número de parâmetros incorreto no método " + m.getId());
			return null;
		}
		//Verifica tipo dos parâmetros
		for(int i = 0; i < n.el.size(); i++){
			if(!symbolTable.compareTypes(n.el.elementAt(i).accept(this), m.getParamAt(i).type())){
				error.complain("Erro no tipo do parâmetro do método " + m.getId());
				return null;
			}
		}

		return m.type();

	}

	// int i;
	public Type visit(IntegerLiteral n) {
		return new IntegerType();
	}

	public Type visit(True n) {
		return new BooleanType();
	}

	public Type visit(False n) {
		return new BooleanType();
	}

	// String s;
	public Type visit(IdentifierExp n) {
		return symbolTable.getVarType(currMethod, currClass, n.s);
	}

	public Type visit(This n) {
		return currClass.type();
	}

	// Exp e;
	public Type visit(NewArray n) {
		Type t = n.e.accept(this);
		if(t == null){
			return null;
		}
		// ?
		if(!symbolTable.compareTypes(t, new IntegerType())){
			error.complain("Tipo de " + n.e.toString() + " deve ser Integer");
			return null;
		}
		return new IntArrayType();
	}

	// Identifier i;
	public Type visit(NewObject n) {
		Type t = n.i.accept(this);
		if(t == null){
			return null;
		}
		String id = n.i.s;
		if(!(symbolTable.containsClass(id))){
			error.complain("Classe " + id + " não foi existe");
			return null;
		}
		return new IdentifierType(id);
	}

	// Exp e;
	public Type visit(Not n) {
		if(n == null){
			return null;
		}
		Type e = n.e.accept(this);
		if(!(e instanceof BooleanType)){
			error.complain("Expressão " + n.e.toString() + " não é do tipo Boolean");
			return null;
		}
		return new BooleanType();
	}

	// String s;
	public Type visit(Identifier n) {
		if(n == null){
			return null;
		}
		String id = n.s;
		Variable v = null;
		if(currMethod != null){
			v = currMethod.getParam(id);
			if(v == null){
				v = currMethod.getVar(id);
			}
			if(v == null){
				v = currClass.getVar(id);
			}
		} else if(currClass != null){
			v = currClass.getVar(id);
		}
		if(v == null){
			return null;
		}
		return v.type();
	}
}
