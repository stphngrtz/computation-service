grammar ComputationExpressionLanguage;

expression: (NUMBER | VARIABLE) ((PLUS | MINUS) (NUMBER | VARIABLE))* ;

NUMBER: '-'? DIGIT+ ('.' DIGIT+)? ;
VARIABLE: LETTER (LETTER | DIGIT)* ;

PLUS: '+' ;
MINUS: '-' ;

fragment LETTER : [a-zA-Z_] ;
fragment DIGIT: [0-9] ;

WS: [ \r\n\t]+ -> channel(HIDDEN) ;
