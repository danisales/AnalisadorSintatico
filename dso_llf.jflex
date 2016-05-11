package cup;

import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.ComplexSymbolFactory.Location;
import java_cup.runtime.Symbol;
import java.lang.*;
import java.io.InputStreamReader;

%%

%line
%column
%cup
%unicode
%standalone
%implements sym
%char

%class Lexer

%{	
    public Lexer(ComplexSymbolFactory sf, java.io.InputStream is){
		this(is);
        symbolFactory = sf;
    }
	public Lexer(ComplexSymbolFactory sf, java.io.Reader reader){
		this(reader);
        symbolFactory = sf;
    }
    
    private StringBuffer sb;
    private ComplexSymbolFactory symbolFactory;
    private int csline,cscolumn;

    public Symbol symbol(String name, int code){
		return symbolFactory.newSymbol(name, code,
						new Location(yyline+1,yycolumn+1, yychar), // -yylength()
						new Location(yyline+1,yycolumn+yylength(), yychar+yylength())
				);
    }
    public Symbol symbol(String name, int code, String lexem){
	return symbolFactory.newSymbol(name, code, 
						new Location(yyline+1, yycolumn +1, yychar), 
						new Location(yyline+1,yycolumn+yylength(), yychar+yylength()), lexem);
    }
    
    protected void emit_warning(String message){
    	System.out.println("scanner warning: " + message + " at : 2 "+ 
    			(yyline+1) + " " + (yycolumn+1) + " " + yychar);
    }
    
    protected void emit_error(String message){
    	System.out.println("scanner error: " + message + " at : 2" + 
    			(yyline+1) + " " + (yycolumn+1) + " " + yychar);
    }
%}


/* Identificadores: um identificador começa com uma letra ou underline e é seguido por qualquer quantidade de letras, underline e dígitos.
Apenas letras entre A/a e Z/z são permitidos, há diferença entre maiúscula e minúscula.
Palavras-chave não são identificadores; */

/* Literais Inteiros: uma sequência de dígitos iniciada com qualquer um dos dígitos entre 1 e 9 e seguida por qualquer número de dígitos entre 0 e 9.
O dígito 0 também é um inteiro. */

letters = [A-Za-z]
underline = [_]
digit = [0-9]
integer = {digit}+
first = {letters} | {underline}
alphanumerics = {letters}|{digit}|{underline}
identifiers = {first}({alphanumerics})*
whitespace = {linebreak}|[ \t\f]
linebreak = \r|\n|\r\n

/* Comentários: os dois tipos de comentário são possíveis, comentários de linha, iniciando com // e indo até o final da linha, e comentários de múltiplas linhas, que consistem de qualquer texto entre /* e */, sem considerar aninhamento */

comment = {multilinecomment} | {onelinecomment}
multilinecomment = "/*" ~"*/"
onelinecomment = "//" ~{linebreak}

%eofval{
    return symbolFactory.newSymbol("EOF",sym.EOF);
%eofval}

%state CODESEG


%%
/* Operadores: &&, <, ==, !=, +, -, *, !; 
(não há operador de divisão, por enquanto) */
"&&"	{ return symbolFactory.newSymbol("AND", AND); }
"<"		{ return symbolFactory.newSymbol("LESST", LESST); }
"=="	{ return symbolFactory.newSymbol("EQUALOP", EQUALOP); }
"!="	{ return symbolFactory.newSymbol("NOTEQUAL", NOTEQUAL); }
"+"		{ return symbolFactory.newSymbol("PLUS", PLUS); }
"-"		{ return symbolFactory.newSymbol("MINUS", MINUS); }
"*"		{ return symbolFactory.newSymbol("TIMES", TIMES); }
"!"		{ return symbolFactory.newSymbol("NOT", NOT); }

/* Delimitadores e pontuação: ; . , = ( ) { } [ ] */

";"		{ return symbolFactory.newSymbol("SEMI", SEMI); }
"."		{ return symbolFactory.newSymbol("DOT", DOT); }
","		{ return symbolFactory.newSymbol("COMMA", COMMA); }
"="		{ return symbolFactory.newSymbol("EQUAL", EQUAL); }
"("		{ return symbolFactory.newSymbol("LPAREN", LPAREN); }
")"		{ return symbolFactory.newSymbol("RPAREN", RPAREN); }
"{"		{ return symbolFactory.newSymbol("LBRACE", LBRACE); }
"}"		{ return symbolFactory.newSymbol("RBRACE", RBRACE); }
"["		{ return symbolFactory.newSymbol("LBRACKET", LBRACKET); }
"]"		{ return symbolFactory.newSymbol("RBRACKET", RBRACKET); }

/* Palavras reservadas: boolean, class, public, extends, static, void, main, String, int, while,
if, else, return, length, true, false, this, new, System.out.println; */

boolean	{ return symbolFactory.newSymbol("BOOL", BOOL); }
class	{ return symbolFactory.newSymbol("CLASS", CLASS); }
public	{ return symbolFactory.newSymbol("PUBLIC", PUBLIC); }
extends	{ return symbolFactory.newSymbol("EXTENDS", EXTENDS); }
static	{ return symbolFactory.newSymbol("STATIC", STATIC); }
void	{ return symbolFactory.newSymbol("VOID", VOID); }
main	{ return symbolFactory.newSymbol("MAIN", MAIN); }
String	{ return symbolFactory.newSymbol("STRING", STRING); }
int 	{ return symbolFactory.newSymbol("INT", INT); }
while	{ return symbolFactory.newSymbol("WHILE", WHILE); }
if		{ return symbolFactory.newSymbol("IF", IF); }
else	{ return symbolFactory.newSymbol("ELSE", ELSE); }
return	{ return symbolFactory.newSymbol("RETURN", RETURN); }
length	{ return symbolFactory.newSymbol("LENGTH", LENGTH); }
true	{ return symbolFactory.newSymbol("TRUE", TRUE); }
false	{ return symbolFactory.newSymbol("FALSE", FALSE); }
this	{ return symbolFactory.newSymbol("THIS", THIS); }
new		{ return symbolFactory.newSymbol("NEW", NEW); }
System.out.println	{ return symbolFactory.newSymbol("SYSO", SYSO); }

{integer}		{ return symbolFactory.newSymbol("INTEGER", INTEGER, Integer.parseInt(yytext())); }
{identifiers}	{ return symbolFactory.newSymbol("ID", ID, yytext()); }

/* Comentários e whitespace não tem significado algum, exceto para separar os tokens. */
{comment} 	 { /* ignora */ }
{whitespace} { /* ignora */ }

. 			 { throw new RuntimeException("Caractere ilegal! '" + yytext() + "' na linha: " + yyline + ", coluna: " + yycolumn); }
