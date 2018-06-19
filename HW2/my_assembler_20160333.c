/*
 * 화일명 : my_assembler_00000000.c 
 * 설  명 : 이 프로그램은 SIC/XE 머신을 위한 간단한 Assembler 프로그램의 메인루틴으로,
 * 입력된 파일의 코드 중, 명령어에 해당하는 OPCODE를 찾아 출력한다.
 * 파일 내에서 사용되는 문자열 "00000000"에는 자신의 학번을 기입한다.
 */

/*
 *
 * 프로그램의 헤더를 정의한다. 
 *
 */

#include <stdio.h>	
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>
#include <stdlib.h>

// 파일명의 "00000000"은 자신의 학번으로 변경할 것.
#include "my_assembler_20160333.h"

/* ----------------------------------------------------------------------------------
 * 설명 : 사용자로 부터 어셈블리 파일을 받아서 명령어의 OPCODE를 찾아 출력한다.
 * 매계 : 실행 파일, 어셈블리 파일 
 * 반환 : 성공 = 0, 실패 = < 0 
 * 주의 : 현재 어셈블리 프로그램의 리스트 파일을 생성하는 루틴은 만들지 않았다. 
 *		   또한 중간파일을 생성하지 않는다. 
 * ----------------------------------------------------------------------------------
 */
int main(int args, char *arg[]) 
{
    if(init_my_assembler()< 0)
    {
	  printf("init_my_assembler: 프로그램 초기화에 실패 했습니다.\n"); 
	  return -1 ; 
    }

    if(assem_pass1() < 0 ){
	  printf("assem_pass1: 패스1 과정에서 실패하였습니다.  \n") ; 
	  return -1 ; 
    }


    for(int i=0;i<sym_line;++i)
	  printf("%s\t\t%X\n",sym_table[i].symbol,sym_table[i].addr);


    make_symtab_output("symtab_20160333");	// symbol 테이블을 파일에 저장합니다.

    if(assem_pass2() < 0 ){
	  printf(" assem_pass2: 패스2 과정에서 실패하였습니다.  \n") ; 
	  return -1 ; 
    }

    make_objectcode_output("output_20160333");		// objectcode 파일을 생성합니다. 


    memoryFree();		// 메모리를 해제합니다.

    return 0;

}

// 메모리 할당을 해제시켜주는 함수이다.
void memoryFree(void){
    for(int i=0;i<inst_index;++i){
	  if(inst_table[i]->mnemonic!=NULL)
		free(inst_table[i]->mnemonic);
	  if(inst_table[i]->countOperand!=NULL);
	  free(inst_table[i]->countOperand);
	  if(inst_table[i]->format!=NULL)
		free(inst_table[i]->format);
	  if(inst_table[i]->opcods!=NULL)
		free(inst_table[i]->opcods);

	  free(inst_table[i]);
    }
    for(int i=0;i<token_line;++i){
	  if(token_table[i]->label!=NULL)
		free(token_table[i]->label);
	  if(token_table[i]->operator!=NULL)
		free(token_table[i]->operator);
	  if(token_table[i]->comment!=NULL)
		free(token_table[i]->comment);
	  for(int j=0;j<MAX_OPERAND;++j)
		if(token_table[i]->operand[j]!=NULL)
		    free(token_table[i]->operand[j]);
	  free(token_table[i]);
    }
}


/* ----------------------------------------------------------------------------------
 * 설명 : 프로그램 초기화를 위한 자료구조 생성 및 파일을 읽는 함수이다. 
 * 매계 : 없음
 * 반환 : 정상종료 = 0 , 에러 발생 = -1
 * 주의 : 각각의 명령어 테이블을 내부에 선언하지 않고 관리를 용이하게 하기 
 *		   위해서 파일 단위로 관리하여 프로그램 초기화를 통해 정보를 읽어 올 수 있도록
 *		   구현하였다. 
 * ----------------------------------------------------------------------------------
 */
int init_my_assembler(void)
{
    int result ; 

    if((result = init_inst_file("inst.data")) < 0 )
	  return -1 ;
    if((result = init_input_file("input.txt")) < 0 )
	  return -1 ; 
    return result ; 
}

/* ----------------------------------------------------------------------------------
 * 설명 : 머신을 위한 기계 코드목록 파일을 읽어 기계어 목록 테이블(inst_table)을 
 *        생성하는 함수이다. 
 * 매계 : 기계어 목록 파일
 * 반환 : 정상종료 = 0 , 에러 < 0 
 * 주의 : 기계어 목록파일 형식은 자유롭게 구현한다. 예시는 다음과 같다.
 *	
 *	===============================================================================
 *		   | 이름 | 형식 | 기계어 코드 | 오퍼랜드의 갯수 | NULL|
 *	===============================================================================	   
 *		
 * ----------------------------------------------------------------------------------
 */
int init_inst_file(char *inst_file)
{
    FILE * file;
    int errno=0;
    char temp[256]={-1,};
    char *pch;

    /* add your code here */

    if((file=fopen(inst_file,"r"))==NULL)
	  return -1;

    for(int i=0;i<MAX_INST;++i ){
	  // inst.data 에서 한 줄 씩 입력받아 temp 배열에 저장한다.
	  fgets(temp,sizeof(temp),file);	
	  if(feof(file))
		break;
	  //inst_table배열 에서 index 0 부터 동적할당 해준다.
	  inst_table[i]=(inst *)malloc(sizeof(inst));	
	  // 읽어온 instruction을 strcpy 함수를 이용하여 토큰을 나눠 저장한다.

	  pch = strtok(temp,"\t ");
	  // mnemonic 변수에 동적할당하여 토큰을 복사한다.
	  inst_table[i]->mnemonic = (char *)malloc(strlen(pch));
	  strcpy(inst_table[i]->mnemonic,pch);

	  // countOperand 변수에 동적할당하여 토큰을 복사한다.
	  pch = strtok(NULL,"\t ");
	  inst_table[i]->countOperand =(char *)malloc(strlen(pch));
	  strcpy(inst_table[i]->countOperand,pch);

	  // format 변수에 동적할당하여 토큰을 복사한다.
	  pch = strtok(NULL,"\t ");
	  inst_table[i]->format = (char *)malloc(strlen(pch));
	  strcpy(inst_table[i]->format,pch);

	  // opcods 변수에 동적할당하여 토큰을 복사한다.
	  // opcods 가 마지막 토큰이므로 마지막에 '\0'값을 넣어줘 개행을 없앤다.
	  pch = strtok(NULL,"\t ");
	  pch[strlen(pch)-2]='\0';
	  inst_table[i]->opcods =(char *)malloc(strlen( pch));
	  strcpy(inst_table[i]->opcods,pch);

	  inst_index++;


    }

    return errno;
}

/* ----------------------------------------------------------------------------------
 * 설명 : 어셈블리 할 소스코드를 읽어 소스코드 테이블(input_data)를 생성하는 함수이다. 
 * 매계 : 어셈블리할 소스파일명
 * 반환 : 정상종료 = 0 , 에러 < 0  
 * 주의 : 라인단위로 저장한다.
 *		
 * ----------------------------------------------------------------------------------
 */
int init_input_file(char *input_file)
{
    FILE * file;
    int errno;
    char temp[100];

    /* add your code here */

    if((file=fopen(input_file,"r"))==NULL)
	  return -1;
    for(int i=0;i<MAX_INST;++i){
	  // input_file 에서 한줄씩 입력받아 input_data 배열에 넣어준다.
	  fgets(temp,sizeof(temp),file);
	  if(feof(file))
		break;
	  // input_data 배열에 어셈블리할 소스코드를 저장하기 위해 동적할당한다.
	  input_data[i]=(char *)malloc(strlen(temp));
	  strcpy(input_data[i],temp);
	  line_num++;
    }
    return errno;
}

/* ----------------------------------------------------------------------------------
 * 설명 : 소스 코드를 읽어와 토큰단위로 분석하고 토큰 테이블을 작성하는 함수이다. 
 *        패스 1로 부터 호출된다. 
 * 매계 : 파싱을 원하는 문자열  
 * 반환 : 정상종료 = 0 , 에러 < 0 
 * 주의 : my_assembler 프로그램에서는 라인단위로 토큰 및 오브젝트 관리를 하고 있다. 
 * ----------------------------------------------------------------------------------
 */
int token_parsing(char *str) 
{
    /* add your code here */

    char *temp,temp_str[100];
    int i=0;
    char* buffer;
    token_table[token_line] = (token*)malloc(sizeof(token));
    char *pch=NULL;

    // temp_str 배열에 str 문자열을 저장하여 str이 수정된 뒤에 다시 원래대로 되돌린다.
    strcpy(temp_str,str);
    init_token(token_table[token_line]);

    if(str==NULL)
	  return -1;

    if(str[0]=='.'){		 //.으로 시작하는 주석 무시하고 동적할당 해제한다.
	  free(token_table[token_line]);
	  return 0;
    }

    if(str[0]=='\t'||str[0]==' '){		  //label이 없을 때 NULL 저장한다.
	  token_table[token_line]->label =NULL;
	  // pch 에 operator 저장한다.
	  pch = strtok(str,"\t ");
    }
    else{		  //label이 있을 때
	  //pch 에  label token 저장
	  pch = strtok(str,"\t ");			
	  token_table[token_line]->label =(char *)malloc(strlen(pch));
	  strcpy(token_table[token_line]->label,pch);
	  //label 이후 문장이 없을경우 개행문자를 없애고 함수 종료한다.
	  if(token_table[token_line]->label[strlen(pch)-1]=='\n'){	
		token_table[token_line++]->label[strlen(pch)-2]='\0';
		//수정된 str을 원래상태로 되돌린다.
		strcpy(str,temp_str);
		return 0;
	  }
	  //pch에 operator 저장한다.
	  pch = strtok(NULL, "\t ");
    }

    //operator에 동적할당 하여 토큰을 복사한다.
    token_table[token_line]->operator =(char *)malloc(strlen(pch));
    strcpy(token_table[token_line]->operator,pch);
    //만약 operator에 개행문자가 있을 경우 '\0'로 바꿔준다.
    if(token_table[token_line]->operator[strlen(pch)-1]=='\n'){
	  token_table[token_line++]->operator[strlen(pch)-2]='\0';
	  //수정된 str을 원래상태로 되돌린다.
	  strcpy(str,temp_str);
	  return 0;
    }

    // RSUB 명령어는 operand 가 없기때문에 따로 예외처리 해주었다.
    if(strcmp(pch,"RSUB")==0){
	  //pch에 comment를 저장한다.
	  pch = strtok(NULL,"\t ");		
	  pch[strlen(pch)-2]='\0';
	  //comment 변수에 동적할당 해주고 토큰 복사하여 함수 종료한다.
	  token_table[token_line]->comment = (char *)malloc(strlen(pch));
	  strcpy(token_table[token_line]->comment,pch);
	  token_line++;
	  //수정된 str을 원래상태로 되돌린다.
	  strcpy(str,temp_str);
	  return 0;
    }

    // pch 에 operator 저장한다.
    pch = strtok(NULL,"\t ");

    //operand를 buffer 에 저장한다.
    buffer = (char*)malloc(strlen(pch));
    strcpy(buffer,pch);

    if(pch[strlen(pch)-1]!='\n'){		//comment 가 있을 경우
	  //pch 에 comment 저장한다.
	  pch = strtok(NULL,"\t ");

	  //comment 에 동적할당 해준 후 '\n' 자리에 '\0'로 대체한다.
	  token_table[token_line]->comment = (char *)malloc(strlen(pch));
	  strcpy(token_table[token_line]->comment,pch);
	  token_table[token_line]->comment[strlen(pch)-2]='\0';
    }

    //operand 를  구분자 ','로 하여 토큰화한다.
    temp = strtok(buffer,",");
    while(temp!=NULL){
	  //마지막 토큰이면 '\n'을 없애준다.
	  if(temp[strlen(temp)-1]=='\n')
		temp[strlen(temp)-2]='\0';
	  token_table[token_line]->operand[i] = (char *)malloc(strlen(temp));
	  strcpy(token_table[token_line]->operand[i++],temp);
	  temp = strtok(NULL,",");
    }

    token_line++;
    //수정된 str을 원래상태로 되돌린다.
    strcpy(str,temp_str);
    return 0;


}

void init_token(token *toke){		//inst_unit 구조체를 초기화 해주는 함수이다.
    toke->label = NULL;
    toke->operator = NULL;
    toke->comment = NULL;
    for(int i=0;i<3;++i)
	  toke->operand[i]=NULL;
    toke->nixbpe=-1;
}


/* ----------------------------------------------------------------------------------
 * 설명 : 입력 문자열이 기계어 코드인지를 검사하는 함수이다. 
 * 매계 : 토큰 단위로 구분된 문자열 
 * 반환 : 정상종료 = 기계어 테이블 인덱스, 에러 < 0 
 * 주의 : 
 *		
 * ----------------------------------------------------------------------------------
 */
int search_opcode(char *str) 
{
    /* add your code here */

    char temp[20];
    strcpy(temp,str);

    //입력문자열의 첫번째 문자가 '+'이면 temp 배열에 하나씩 땡겨줘 '+'문자를 없애준다.
    if(str[0]=='+')
	  for(int j=0;j<strlen(temp);++j)
		temp[j]=temp[j+1];

    for(int i=0;i<inst_index;++i){
	  if(strcmp(inst_table[i]->mnemonic,temp)==0)
		return i;
    }
    return -1;


}

// symbol 테이블을 구조체 배열에 저장하는 함수이다.
int setting_symbol_table(){

    token *head=NULL;
    for(int i=0;i<token_line;++i){
	  head=token_table[i];

	  if(head->label!=NULL && strcmp(head->label,"*")!=0){
		strcpy(sym_table[sym_line].symbol,head->label);
		sym_table[sym_line++].addr = address[i];
	  }

    }

    return 0;
}

// symbol 테이블을 파일에 저장하는 함수이다.
void make_symtab_output(char *file_name){
    FILE *fp;

    fp=fopen(file_name,"w");

    for(int i=0;i<sym_line;++i)
	  fprintf(fp,"%s\t\t%d\n",sym_table[i].symbol,sym_table[i].addr);

    fclose(fp);


}

// 각 라인 당 주소를 할당하여 address 배열에 저장하는 함수이다.
int setting_line_address(){

    token *head = NULL;

    for(int i=0;i<token_line;++i)	// 모든 라인 주소를 -1로 초기화 해준다.
	  address[i]=-1;
    for(int i=0;i<token_line;++i){
	  head = token_table[i];

	  //해당 라인의 명령어를 체크하여 다음 주소값을 할당한다.
	  if(head->operator!=NULL && strcmp(head->operator,"START")==0)
		address[i]=0;
	  else if(head->operator !=NULL && strcmp(head->operator,"EXTDEF")==0 )
		set_def(head);									// extdef 배열에 값을 넣는다.
	  else if(head->operator!=NULL && strcmp(head->operator,"EXTREF")==0)
		set_ref(head);									// extref 배열에 값을 넣는다.
	  else if(head->label!=NULL && strcmp(head->label,"FIRST")==0){
		address[i]=address[0];
		address[i+1]=address[i] + operator_format(head);
	  }
	  else if(head->operator!=NULL && strcmp(head->operator,"RESW")==0)
		address[i+1] = address[i]+3*atoi(head->operand[0]);
	  else if(head->operator!=NULL && strcmp(head->operator,"RESB")==0)
		address[i+1] = address[i] + atoi(head->operand[0]);
	  else if(head->operator!=NULL&&strcmp(head->operator,"LTORG")==0){
		set_ltorg_next(i);								// literal들을 LTORG 뒤에 넣는다.
	  }
	  else if(head->label!=NULL && strcmp(head->label,"*")==0){
		address[i+1] = address[i] + strlen_string(head->operator);		// literal의 길이를 구하여 다음 라인 주소를 구한다.
	  }
	  else if(head->label!=NULL && strcmp(head->label,"MAXLEN")==0){	

		address[i] = maxlen_address(head,i);				// MAXLEN의 주소값을 구한다.
	  }
	  else if(head->operator && strcmp(head->operator,"WORD")==0)
		address[i+1] = address[i] + 3;
	  else if(head->label && strcmp(head->operator,"BYTE")==0)
		address[i+1] = address[i] + 1;
	  else if(operator_format(head)==2)
		address[i+1] = address[i]+2;
	  else if(operator_format(head)==3)
		address[i+1] = address[i]+3;
	  else if(head->operator[0]=='+')
		address[i+1] = address[i]+4;
	  else if(head->operator!=NULL &&strcmp(head->operator,"CSECT")==0)
		address[i] = address[i+2] =  0;
	  else if(head->operator!=NULL &&strcmp(head->operator,"END")==0){
		set_end_next(i);
		return 0;
	  }

	  //리터럴이 나왔을 때 리터럴 테이블에 없으면 리터럴을 리터럴 테이블에 넣어준다.
	  if(head->operand[0]!=NULL &&head->operand[0][0]=='='&& find_literal(head->operand[0])==-1){
		strcpy(literal_table[literal_line].symbol,head->operand[0]);
		literal_table[literal_line++].addr = -1;		// 아직 주소를 모르므로 -1으로 초기화한다.
	  }
    }
}

// 해당 sect의 extdef 배열에 값을 넣어준다.
void set_def(token *tk){
    int i=0;
    def_len=0;
    while(tk->operand[i]!=NULL){
	  extdef[def_len]=(char*)malloc(strlen(tk->operand[i]));
	  strcpy(extdef[def_len],tk->operand[i]);
	 def_len++;
	 i++;
    }
}

//해당 sect의 extref 배열에 값을 넣어준다.
void set_ref(token *tk){
    int i=0;
    ref_len=0;
    while(tk->operand[i]!=NULL){
	  extref[ref_len]=(char*)malloc(strlen(tk->operand[i]));
	  strcpy(extref[ref_len],tk->operand[i]);
	   ref_len++;
	   i++;
    }
}

//LTORG 다음에 그 전에 나왔던 리터럴을 token_table에 넣어준다.
void set_ltorg_next(int i){
    for(int j=0;j<literal_line-not_use_literal;++j){	// 리터럴 테이블 중에서 삽입하지 않은 러터럴이 있다면
	  if(literal_line!=0 && not_use_literal==0){	//LTORG 다음에 들어가는 첫 레이블일때
		for(int j=token_line-1;j>i;--j)		// token_table을 하나씩 미룬다.
		    token_table[j+1]=token_table[j];
		input_literal(i);					//token_table에 리터럴을 삽입한다.
		address[i+1]=address[i];
		literal_table[not_use_literal].addr = address[i+1];	// 리터럴 테이블에 해당 라인의 주소값을 넣어준다.
		address[i]=-1;
		not_use_literal++;
	  }
	  else if(literal_line!=0){			// 첫번째 레이블이 아닐 때
		for(int j=token_line-1;j>i;--j)
		    token_table[j+1]=token_table[j];
		input_literal(i+not_use_literal);
		address[i+1] = address[i] + strlen_string(literal_table[not_use_literal].symbol);
		not_use_literal++;
	  }
    }

}

//end 다음에 레이블을 넣는 함수이다.
void set_end_next(int i){
    int temp=0;
    temp = literal_line-not_use_literal;
    for(int j=0;j<temp;++j){
	  if(literal_table[not_use_literal].symbol[1]=='X')		// 레이블이 1byte 인 경우
		locctr=1;
	  else if(literal_table[not_use_literal].symbol[1]=='C')
		locctr = strlen_string(literal_table[not_use_literal].symbol);

	  if(j==0){				//end 다음에 넣는 레이블인 경우
		input_literal(i);
		address[i+1] = address[i];
		address[i]=-1;
		address[i+2]=address[i+1] + locctr;
		literal_table[not_use_literal].addr = address[i+1];

	  }
	  else{				// end 뒤 첫번째 레이블이 아닌 경우
		input_literal(i+j);
		address[i+2+j] = address[i+j+1] + locctr;
		literal_table[not_use_literal].addr = address[i+1+j];
	  }
	  not_use_literal++;
    }


}

// token_table에 레이블을 넣어준다.
void input_literal(int i){

    token_table[i+1]=(token*)malloc(sizeof(token));
    token_table[i+1]->label = (char*)malloc(sizeof(char)*2);
    strcpy(token_table[i+1]->label,"*");
    token_table[i+1]->operator = (char *)malloc(strlen(literal_table[not_use_literal].symbol)*sizeof(char));
    strcpy(token_table[i+1]->operator,literal_table[not_use_literal].symbol);

    // label 과 operator 이외의 요소는 NULL 로 초기화한다.
    for(int k=0;k<3;++k)
	  token_table[i+1]->operand[k]=NULL;
    token_table[i+1]->comment=NULL;
    token_line++;



}

// MAXLEN의 주소값을 구한다.
int maxlen_address(token *tk,int k){
    int result=0;
    char *pch;

    char temp[100]="";
    strcpy(temp,tk->operand[0]);
    pch = strtok(temp,"-");		// MAXLEN의 operand를 '-'구분자를 사용하여 토큰을 나눈다.
    maxlen_token[0] = (char*)malloc(sizeof(char)*strlen(pch));	//나눈 토큰을 전역변수 maxlen_token 배열에 넣는다.
    strcpy(maxlen_token[0],pch);
    pch = strtok(NULL,"-");
    maxlen_token[1] = (char*)malloc(sizeof(char)*strlen(pch));
    strcpy(maxlen_token[1],pch);

    for(int i=0;i<ref_len;++i)		// maxlen이 해당 sect에서 정의되지 않은 경우
	  for(int j=0;j<2;++j)
		if(strcmp(extref[0],maxlen_token[j])==0)
		    return address[k];
    for(int i=0;i<token_line;++i){		// maxlen이 해당 sect에서 정의된 경우
	  if(token_table[i]->label!=NULL){
		if(strcmp(token_table[i]->label,maxlen_token[0])==0){
		    result += address[i];
		}
		else if(strcmp(token_table[i]->label,maxlen_token[1])==0)
		    result-=address[i];
	  }
    }


    return result;
}

//해당 리터럴이 리터럴 테이블에 있는지 검사한다.
int find_literal(char *str){
    int result=-1;
    for(int i=0;i<literal_line;++i){
	  if(strcmp(literal_table[i].symbol,str)==0)
		result=i;		//리터럴 테이블에 있다면 index를 리턴한다.

	  else
		result=-1;;
    }
    return result;
}

//해당 명령어의 형식을 리턴하는 함수이다.
int operator_format(token *tk){
    int result=0;
    for(int i=0;i<inst_index;++i)
	  if(strcmp(tk->operator,inst_table[i]->mnemonic)==0){
		if(strcmp(inst_table[i]->format,"3/4")==0)	// 3형식인 경우
		    result = 3;
		else if(tk->operator[0]=='+')				// 4형식인 경우
		    result=4;
		else
		    result = atoi(inst_table[i]->format);		// 2형식인 경우
		break;
	  }

    return result;

}

//리터럴에서 =' '를 제외한 글자 수를 리턴한다.
int strlen_string(char *str){
    for(int i=0;i<strlen(str);++i)
	  if(str[i]=='\''){
		++i;
		for(int j=0;j<10;++j){
		    string[j]=str[i++];		//메모리에 올릴 문자를 string 배열에 저장한다.
		    if(str[i]=='\''){
			  string[j+1]='\0';
			  break;
		    }
		}
		break;
	  }

    return strlen(string);

}

/* ----------------------------------------------------------------------------------
 * 설명 : 어셈블리 코드를 위한 패스1과정을 수행하는 함수이다.
 *		   패스1에서는..
 *		   1. 프로그램 소스를 스캔하여 해당하는 토큰단위로 분리하여 프로그램 라인별 토큰
 *		   테이블을 생성한다.
 *
 * 매계 : 없음
 * 반환 : 정상 종료 = 0 , 에러 = < 0
 * 주의 : 현재 초기 버전에서는 에러에 대한 검사를 하지 않고 넘어간 상태이다.
 *	  따라서 에러에 대한 검사 루틴을 추가해야 한다.
 *
 * -----------------------------------------------------------------------------------
 */
static int assem_pass1(void)
{
    /* add your code here */

    /* input_data의 문자열을 한줄씩 입력 받아서 
     * token_parsing()을 호출하여 token_unit에 저장
     */
    for(int i=0;i<line_num;++i)
	  if(token_parsing(input_data[i])==-1)
		return -1;

 setting_line_address();	// 각 라인별 주소를 address 배열에 할당합니다.

    setting_symbol_table();	// symbol 테이블을 구조체 배열에 관리합니다.

    return 0;




}


/* ----------------------------------------------------------------------------------
 * 설명 : 입력된 문자열의 이름을 가진 파일에 프로그램의 결과를 저장하는 함수이다.
 *        여기서 출력되는 내용은 명령어 옆에 OPCODE가 기록된 표(과제 4번) 이다.
 * 매계 : 생성할 오브젝트 파일명
 * 반환 : 없음
 * 주의 : 만약 인자로 NULL값이 들어온다면 프로그램의 결과를 표준출력으로 보내어
 *        화면에 출력해준다.
 *        또한 과제 4번에서만 쓰이는 함수이므로 이후의 프로젝트에서는 사용되지 않는다.
 * -----------------------------------------------------------------------------------
 */
void make_opcode_output(char *file_name)
{
    /* add your code here */
    FILE *file;
    token *head=NULL;
    int k=0;
    char temp[20];

    if((file=fopen(file_name,"w"))==NULL){
	  printf("파일을 열지 못하였습니다.\n");
	  return ;
    }

    // token_line만큼 token_table 배열 각 방마다의 각 요소들을 output 파일에 출력한다.
    for(int i=0;i<token_line;++i){
	  head = token_table[i];
	  //label이 없으면 탭만 출력한다.
	  if(head->label==NULL)
		fprintf(file,"\t");
	  else
		fprintf(file,"%s\t",head->label);

	  //operator가 없으면 탭만 출력한다.
	  if(head->operator==NULL)
		fprintf(file,"\t");
	  else
		fprintf(file,"%s\t",head->operator);

	  // operator 출력한다.
	  for(int j=0;j<3;++j){
		//operand가 더이상 없으면 탭 출력한다.
		if(head->operand[j]==NULL){
		    fprintf(file,"\t");
		    break;
		}
		else{
		    if(j==0)	// 처음출력하는 operand
			  fprintf(file,"%s",head->operand[j]);
		    else		// 두번째부터는  operand앞에 ','출력한다.
			  fprintf(file,",%s",head->operand[j]);
		}
	  }


	  k = search_opcode(head->operator);
	  if(k!=-1)
		fprintf(file,"%s\n",inst_table[k]->opcods);
	  else	 //명령어가 아닐 경우 개행을 출력한다.
		fprintf(file,"\n");
    }

}



/* --------------------------------------------------------------------------------*
 * ------------------------- 추후 프로젝트에서 사용할 함수 --------------------------*
 * --------------------------------------------------------------------------------*/


/* ----------------------------------------------------------------------------------
 * 설명 : 어셈블리 코드를 기계어 코드로 바꾸기 위한 패스2 과정을 수행하는 함수이다.
 *		   패스 2에서는 프로그램을 기계어로 바꾸는 작업은 라인 단위로 수행된다.
 *		   다음과 같은 작업이 수행되어 진다.
 *		   1. 실제로 해당 어셈블리 명령어를 기계어로 바꾸는 작업을 수행한다.
 * 매계 : 없음
 * 반환 : 정상종료 = 0, 에러발생 = < 0
 * 주의 :
 * -----------------------------------------------------------------------------------
 */
static int assem_pass2(void)
{

    /* add your code here */
    token *head=NULL;
    int k=0;
    for(int i=0;i<token_line;++i){
	  head= token_table[i];
	  object_table[i].op[0]=object_table[i].op[1]=object_table[i].op[2]='\0';

	  if((k=search_opcode(head->operator))!=-1){			// inst_table에 있는 명령어일 경우
		object_table[i].opcode = inst_table[k]->opcods[0];	//object_table의 opcode에 해당 명령어의 opcode 앞 4bit를 넣는다.

		head->nixbpe = inst_table[k]->opcods[1];			//token_table의 nixbpe에 해당 명령어의 opcode 뒤 4bit를 넣는다.

		if(operator_format(head)==2){		//명령어가 2형식일 경우

		    set_nixbpe_format2(head);		// 2형식 경우 nixbpe 셋팅

		    for(int j=0;j<9;++j){		// 레지스터의 번호를 op 배열에 넣어준다.
			  if(strcmp(registers[j].name,head->operand[0])==0)
				object_table[i].op[1]=registers[j].number;
			  if(head->operand[1]!=NULL && strcmp(registers[j].name,head->operand[1])==0)
				object_table[i].op[2]=registers[j].number;
		    }


		    object_table[i].op[0]=head->nixbpe>>4;	// 명령어의 opcode 앞부분 4bit 를 op배열 0번째 방에 저장한다.	
		    object_table[i].op[0]&=0x0F;			// 부호 비트가 1이면 값이 이상하기 때문에 마스크 연산을 해준다.
		}
		else{						// 명령어가 3,4형식일 경우

		    set_nixbpe(head);			// 해당 명령어의 nixbpe를 설정한다.

		    object_table[i].op[0] = head->nixbpe >>4;		// nixbpe를 4bit씩 끊어서 op 배열에 저장하여 출력하기 쉬운 상태로 바꾼다.
		    object_table[i].op[0]&=0x0F;				// 부호비트가 있을 경우를 대비해 마스크 연산을 사용한다.
		    object_table[i].op[1] = head->nixbpe & 0x0F;	// nixbpe를 4bit씩 끊어서 op 배열에 저장하여 출력하기 쉬운 상태로 바꾼다.

		    object_table[i].address = cal_address(head,i);	// target address 에서 pc 를 뺀 값을 object_table의 address에 저장한다.
		}



	  }
	  else if(head->label!=NULL &&strcmp(head->label,"*")==0){
		int len = strlen_string(head->operator);
		for(int m=0;m<len;++m)
		    object_table[i].op[m]=string[m];		// object_table 의 op 배열에 메모리에 올릴 문자열을 저장한다.
	  }
	  else if(head->operator!=NULL &&strcmp(head->operator,"BYTE")==0){
		int len = strlen_string(head->operand[0]);
		head->nixbpe=0;
		for(int m=0;m<len;++m)
		    object_table[i].op[m]=string[m];		// object_table의 op 배열에 메모리에 올릴 문자열을 저장한다.
	  }
	  else if(head->operator!=NULL && strcmp(head->operator,"WORD")==0 && strcmp(head->label,"MAXLEN")==0){
		head->nixbpe=0;
		for(int m=0;m<3;++m)
		    object_table[i].op[m]=0;				// maxlen이 상대주소일 경우 0을 저장한다.
	  }



    }
    return 0;


}

// 해당 3,4형식  명령어의 token_table의 nixbpe를 설정해주는 함수이다.
void set_nixbpe(token *tk){

    char bits=0;

    // nixbpe 앞 2bit를 설정해준다.
    if(tk->nixbpe== '0')
	  tk->nixbpe=0x00;
    else if(tk->nixbpe == '4')
	  tk->nixbpe=0x01;
    else if(tk->nixbpe == '8')
	  tk->nixbpe=0x02;
    else if(tk->nixbpe=='C')
	  tk->nixbpe=0x03;

    tk->nixbpe<<=6;		// 마스크 연산을 이용하여 왼쪽 끝으로 보낸다.


    //nibpe를 각 addressing mode에 맞게 설정해준다.
    if(tk->operand[0]!=NULL && tk->operator!=NULL){
	  if(tk->operand[0][0]=='#')
		bits=0x10;
	  else if(tk->operand[0][0]=='@')
		bits=0x22;
	  else if(tk->operator[0]=='+')
		bits=0x31;
	  else
		bits=0x32;

	  tk->nixbpe |= bits;	// or 논리 연산을 이용하여 nixbpe에 저장한다.

	  //x bit를 해당 명령어에 맞게 setting 해준다.
	  if(tk->operand[1]!=NULL &&tk->operand[1][0]=='X')
		tk->nixbpe|=0x08;
	  else
		tk->nixbpe|=0x00;


    }
    else if(strcmp(tk->operator,"RSUB")==0)	//RSUB인 경우만 따로 예외처리 해주었다.
	  tk->nixbpe|=0x30;

}

// 2형식일 경우 nixbpe이의 앞 2bit를 셋팅하는 함수이다.
void set_nixbpe_format2(token *tk){

    // nixbpe의 앞 2bit를 셋팅한다.
    if(tk->nixbpe== '0')
	  tk->nixbpe=0x00;
    else if(tk->nixbpe == '4')
	  tk->nixbpe=0x01;
    else if(tk->nixbpe == '8')
	  tk->nixbpe=0x02;
    else if(tk->nixbpe=='C')
	  tk->nixbpe=0x03;

    tk->nixbpe<<=6;

}

// target address - pc 를 계산해주는 함수이다.
int  cal_address(token *tk,int index){
    int format=0;
    int i=0;
    int result=0;
    int maxlen_count=0;
    char *sym = tk->operand[0];
    i= operator_format(tk);

    if(tk->operand[0]==NULL)
	  return 0;

    if(i==3){					// 명령어가 3형식인 경우

	  if(tk->operand[0][0]=='@')		//operand에 @가 잇을 경우 포인터를 한칸 미뤄 명령어가 정상적으로 비교될 수 있게 한다.
		sym+=1;

	  for(int j=0;j<sym_line;++j){
		if(strcmp(sym_table[j].symbol,sym)==0){
		    result = sym_table[j].addr - address[index+1];	// target address - pc 를 계산한다.
		    if(strcmp(sym_table[j].symbol,"MAXLEN")==0 &&sym_table[j].addr+address[index]>4096)	// maxlen 절대주소일 경우 넘어간다.
			  continue;
		    if(result<0)				//결과값이 음수일 경우 마스크 연산함으로써 결과값을 출력하는데 오류가 없게 한다.
			  result&=0x00000FFF;
		    return result;
		}

	  }


	  for(int j=0;j<literal_line;++j)		// 명령어 table에 없고 literal_table에 있을 경우
		if(strcmp(literal_table[j].symbol,sym)==0){
		    result = literal_table[j].addr - address[index+1];	//주소값을 계산한다.
		    return result;
		}

	  if(tk->operand[0][0]=='#'){			//immediate 연산일 경우 operand에 있는 숫자를 리턴한다.
		result = atoi(tk->operand[0]+1);
		return result;
	  }

    }
    else if(i==4){			//명령어가 4형식인 경우
	  for(int i=0;i<ref_len;++i)
		if(strcmp(extref[i],tk->operand[0])==0)
		    return 0;		// 0을 리턴한다.

    }


    return result;
}


/* ----------------------------------------------------------------------------------
 * 설명 : 입력된 문자열의 이름을 가진 파일에 프로그램의 결과를 저장하는 함수이다.
 *        여기서 출력되는 내용은 object code (프로젝트 1번) 이다.
 * 매계 : 생성할 오브젝트 파일명
 * 반환 : 없음
 * 주의 : 만약 인자로 NULL값이 들어온다면 프로그램의 결과를 표준출력으로 보내어
 *        화면에 출력해준다.
 *
 * -----------------------------------------------------------------------------------
 */
void make_objectcode_output(char *file_name)
{
    /* add your code here */

    FILE *fp;
    int i=0,k=0;
    int next_len=0,count=0;

    if((fp = fopen(file_name,"w"))==NULL){	//만약 인자로 NULL값이 들어온다면 표준출력한다.

	  printf("\n 인자로 NULL값이 들어왔으므로 표준출력합니다.\n");
	  error_print();
	  return ;
    }



    fprintf(fp,"HCOPY  ");					// 프로그램의 첫 헤더를 출력한다.
    fprintf(fp,"%06X",address[0]);
    for(i=0;i<token_line;++i)
	  if(strcmp(token_table[i]->operator,"EQU")==0)
		break;


    fprintf(fp,"%06X\n",address[i]-address[0]);		// 메인 프로그램의 길이를 구한다.

    set_ext(0,fp);						// 메인 프로그램의 extref, extdef 배열에 값을 넣는다.

    fprintf(fp,"T%06X%02X",address[3],count_object(2)/2);	


    for(i=3;i<token_line;++i){
	  if(token_table[i]->nixbpe!=-1){					// 해당 라인의 nixbpe에 설정된 값이 있을 경우

		if(search_opcode(token_table[i]->operator)!=-1){	// 명령어가 inst_table에 있을 경우

		    if(operator_format(token_table[i])==2){		// 명령어가 2형식일 경우

			  if(object_table[i].op[2]!='\0')			// 피연산자가 2개인 명령어
				count+= fprintf(fp,"%c%X%X%X",object_table[i].opcode,object_table[i].op[0],object_table[i].op[1],object_table[i].op[2]);
			  else							// 피연산자가 1개인 명령어
				count+= fprintf(fp,"%c%X%X0",object_table[i].opcode,object_table[i].op[0],object_table[i].op[1]);

		    }									// 명령어가 3,4형식인 경우
		    else if(token_table[i]->nixbpe&0x01==0x01)
			  count+= fprintf(fp,"%c%X%X%05X",object_table[i].opcode,object_table[i].op[0],object_table[i].op[1],object_table[i].address);	//명령어가 4형식인 경우
		    else
			  count+= fprintf(fp,"%c%X%X%03X",object_table[i].opcode,object_table[i].op[0],object_table[i].op[1],object_table[i].address);	//명령어가 3형식인 경우
		}
		else if(token_table[i]->label!=NULL &&strcmp(token_table[i]->label,"*")==0){

		    if(is_before_res==1){								// 리터럴 전에 resw 나 resb가 있을 경우 개행
			  fprintf(fp,"\nT%06X%02X",address[i],count_object(i)/2);
			  is_before_res=0;
		    }
		    int len = strlen_string(token_table[i]->operator);
		    if(token_table[i]->operator!=NULL && token_table[i]->operator[1]=='X')	// 1 byte 일 경우
			  count+=fprintf(fp,"%c%c",object_table[i].op[0],object_table[i].op[1]);
		    else if(token_table[i]->operator[1]=='C')						// 3 byte 일 경우
			  count+=fprintf(fp,"%X%X%X",object_table[i].op[0],object_table[i].op[1],object_table[i].op[2]);
		}
		else if(token_table[i]->operator!=NULL &&strcmp(token_table[i]->operator,"BYTE")==0){
		    int len = strlen_string(token_table[i]->operand[0]);
		    for(int m=0;m<len;++m)
			  count+= fprintf(fp,"%c",object_table[i].op[m]);
		}
		else if(token_table[i]->operator!=NULL && strcmp(token_table[i]->operator,"WORD")==0 && strcmp(token_table[i]->label,"MAXLEN")==0){
		    for(int m=0;m<3;++m)
			  count+= fprintf(fp,"%02X", object_table[i].op[m]);
		}


	  }
	  if(token_table[i]->operator!=NULL && strcmp(token_table[i]->operator,"RESW")==0 || strcmp(token_table[i]->operator,"RESB")==0)
		is_before_res=1;

	  if(i!=token_line-1){							// 다음 명령어의 object code의 길이를 구한다.
		if(token_table[i+1]->operator!=NULL &&token_table[i+1]->operator[0]=='=')
		    next_len=strlen_string(token_table[i+1]->operator);
		else if(token_table[i+1]->operator!=NULL && token_table[i+1]->operator[0]=='+')
		    next_len=4;
		else if(token_table[i+1]->operator!=NULL && strcmp(token_table[i+1]->operator,"BYTE")==0)
		    next_len=1;
		else if(token_table[i+1]->operator!=NULL && strcmp(token_table[i+1]->operator,"WORD")==0)
		    next_len=3;
		else if(token_table[i+1]->operator!=NULL && operator_format(token_table[i+1])==2)
		    next_len=2;
		else if(token_table[i+1]->operator!=NULL && operator_format(token_table[i+1])==3)
		    next_len=3;


		if(count+next_len*2>60){		//object 프로그램의 한 줄에서 명령어의 총 길이가 60을 넘을 경우
		    fprintf(fp,"\nT%06X%02X",address[i+1],count_object(i+1)/2);
		    count=0;
		}
		next_len=0;

	  }

	  if(strcmp(token_table[i]->operator,"CSECT")==0 || i == token_line-1){		// sect를 나눠야 할 경우
		is_before_res=0;
		fprintf(fp,"\n");
		for(int j=start_point;j<i;++j){			// Modification record가 필요한 부분을 찾는다.
		    for(int k=0;k<ref_len;++k)
			  if(token_table[j]->operand[0]!=NULL&& strcmp(token_table[j]->operand[0],extref[k])==0 && strcmp(token_table[j]->operator,"EXTREF")!=0)
				fprintf(fp,"M%06X05+%s\n",address[j]+1,extref[k]);

		    if(token_table[j]->label!=NULL && token_table[j]->operator!=NULL && strcmp(token_table[j]->label,"MAXLEN")==0 && strcmp(token_table[j]->operator,"EQU")!=0){	//MAXLEN 상대주소일 경우 예외 처리

			  for(int k=0;k<ref_len;++k)
				if(strcmp(maxlen_token[0],extref[k])==0)
				    fprintf(fp,"M%06X06+%s\n",address[j],extref[k]);
			  for(int k=0;k<ref_len;++k)
				if(strcmp(maxlen_token[1],extref[k])==0)
				    fprintf(fp,"M%06X06-%s\n",address[j],extref[k]);

		    }
		}
		fprintf(fp,"E%06X\n\n",address[start_point]);

		// 다음 sect의 header record ,define record, refer record, Text record를 저장한다.
		if(i!=token_line-1)
		    fprintf(fp,"H%-6s%06X",token_table[i]->label,address[i]);
		for(int j=i+1;j<token_line;++j){				// 해당 라인의 총 명령어 길이를 구한다.
		    if(token_table[j]->operator !=NULL && strcmp(token_table[j]->operator,"CSECT")==0){
			  fprintf(fp,"%06X\n",address[j-1]+3-address[i]);
			  break;
		    }
		    else if(j == token_line-1)
			  fprintf(fp,"%06X\n",address[j+1]-address[i]);
		}
		set_ext(i,fp);							//  extref,extdef 배열을 구한다.
		if(i!=token_line-1){
		    fprintf(fp,"T%06X%02X",address[start_point],count_object(i+1)/2);

		}
		count=next_len=0;
		start_point =i;



	  }

    }


    fclose(fp);

}

//extdef, extref 배열에 값을 넣어준다.
void set_ext(int index,FILE *fp){
    int i=0,k=0;

    for(int i=0;i<ref_len;++i)
	  free(extref[i]);
    
    for(int i=0;i<def_len;++i)
	  free(extdef[i]);

    def_len = ref_len = 0;

    for(i=index;i<token_line;++i){
	  if(token_table[i]->operand[0]!=NULL)
	  if(token_table[i]->operator!=NULL && strcmp(token_table[i]->operator,"EXTDEF")==0){
		while(token_table[i]->operand[k]){
		    extdef[k]=(char*)malloc(strlen(token_table[i]->operand[k]));
		    strcpy(extdef[k],token_table[i]->operand[k]);
		    def_len++;
		    k++;
		}
		k=0;
	  }

	  if(token_table[i]->operator!=NULL && strcmp(token_table[i]->operator,"EXTREF")==0){
		while(token_table[i]->operand[k]){
		    extref[k]=(char*)malloc(strlen(token_table[i]->operand[k]));
		    strcpy(extref[k],token_table[i]->operand[k]);
		    ref_len++;
		    k++;
		}
		k=0;
		break;
	  }
    }


    if(def_len!=0){				//extdef 가 있을 경우 출력한다.
	  if(fp!=NULL)
		fprintf(fp,"D");
	  else
		printf("D");
	  for(i=0;i<def_len;++i){
		for(int j=0;j<sym_line;++j)
		    if(strcmp(sym_table[j].symbol,extdef[i])==0){
			  if(fp==NULL)
				printf("%-6s%06X",extdef[i],sym_table[j].addr);
			  else
				fprintf(fp,"%-6s%06X",extdef[i],sym_table[j].addr);
		    }
	  }
	  if(fp==NULL)
		printf("\n");
	  else
		fprintf(fp,"\n");
    }


    if(ref_len!=0){				//extref가 있을 경우 출력한다.
	  if(fp!=NULL)
		fprintf(fp,"R");
	  else
		printf("R");
	  for(i=0;i<ref_len;i++){
		if(fp!=NULL)
		    fprintf(fp,"%-6s",extref[i]);
		else
		    printf("%-6s",extref[i]);
	  }
	
	  if(fp!=NULL)
		fprintf(fp,"\n");
	  else
		printf("\n");

    }
}


// Text record의 한 줄의 명령어 길이를 구하는 함수이다.
int count_object(int index){
    int count=0;
    int next_len=0;

    for(int i=index;i<token_line;++i){
	  if(token_table[i]->operator!=NULL &&token_table[i]->operator[0]=='=' && token_table[i]->operator[1]=='C')
		next_len=strlen_string(token_table[i]->operator)*2;
	  else if(token_table[i]->operator!=NULL && token_table[i]->operator[0]=='=' && token_table[i]->operator[1]=='X')
		next_len=strlen_string(token_table[i]->operator);
	  else if(token_table[i]->operator!=NULL && token_table[i]->operator[0]=='+')
		next_len=8;
	  else if(token_table[i]->operator!=NULL && strcmp(token_table[i]->operator,"BYTE")==0)
		next_len=2;
	  else if(token_table[i]->operator!=NULL && strcmp(token_table[i]->operator,"WORD")==0)
		next_len=6;
	  else if(token_table[i]->operator!=NULL && operator_format(token_table[i])==2)
		next_len=4;
	  else if(token_table[i]->operator!=NULL && operator_format(token_table[i])==3)
		next_len=6;


	  if(count+next_len>60){	// 다음 명령어 길이 합이 60보다 크면 리턴한다.
		return count;
	  }
	  else if(token_table[i]->operator!=NULL && strcmp(token_table[i]->operator,"CSECT")==0)
		return count;
	  else if(token_table[i]->operator!=NULL && strcmp(token_table[i]->operator,"RESW")==0 || strcmp(token_table[i]->operator,"RESB")==0)	//resw,resb 이면 count 값을 리턴한다.
		return count;
	  else
		count+=next_len;

	  next_len=0;
    }

    return count;

}

// file_name에 NULL이 들어올 때 표준출력해주는 함수이다.
void error_print(){
    int i=0,k=0;
    int next_len=0,count=0;
    FILE *fp=NULL;

    printf("HCOPY  ");					// 프로그램의 첫 헤더를 출력한다.
    printf("%06X",address[0]);
    for(i=0;i<token_line;++i)
	  if(strcmp(token_table[i]->operator,"EQU")==0)
		break;


    printf("%06X\n",address[i]-address[0]);		// 메인 프로그램의 길이를 구한다.

    set_ext(0,fp);						// 메인 프로그램의 extref, extdef 배열에 값을 넣는다.

    printf("T%06X%02X",address[3],count_object(2)/2);	


    for(i=3;i<token_line;++i){
	  if(token_table[i]->nixbpe!=-1){					// 해당 라인의 nixbpe에 설정된 값이 있을 경우

		if(search_opcode(token_table[i]->operator)!=-1){	// 명령어가 inst_table에 있을 경우

		    if(operator_format(token_table[i])==2){		// 명령어가 2형식일 경우

			  if(object_table[i].op[2]!='\0')			// 피연산자가 2개인 명령어
				count+= printf("%c%X%X%X",object_table[i].opcode,object_table[i].op[0],object_table[i].op[1],object_table[i].op[2]);
			  else							// 피연산자가 1개인 명령어
				count+= printf("%c%X%X0",object_table[i].opcode,object_table[i].op[0],object_table[i].op[1]);

		    }									// 명령어가 3,4형식인 경우
		    else if(token_table[i]->nixbpe&0x01==0x01)
			  count+= printf("%c%X%X%05X",object_table[i].opcode,object_table[i].op[0],object_table[i].op[1],object_table[i].address);	//명령어가 4형식인 경우
		    else
			  count+= printf("%c%X%X%03X",object_table[i].opcode,object_table[i].op[0],object_table[i].op[1],object_table[i].address);	//명령어가 3형식인 경우
		}
		else if(token_table[i]->label!=NULL &&strcmp(token_table[i]->label,"*")==0){

		    if(is_before_res==1){								// 리터럴 전에 resw 나 resb가 있을 경우 개행
			  printf("\nT%06X%02X",address[i],count_object(i)/2);
			  is_before_res=0;
		    }
		    int len = strlen_string(token_table[i]->operator);
		    if(token_table[i]->operator!=NULL && token_table[i]->operator[1]=='X')	// 1 byte 일 경우
			  count+=printf("%c%c",object_table[i].op[0],object_table[i].op[1]);
		    else if(token_table[i]->operator[1]=='C')						// 3 byte 일 경우
			  count+=printf("%X%X%X",object_table[i].op[0],object_table[i].op[1],object_table[i].op[2]);
		}
		else if(token_table[i]->operator!=NULL &&strcmp(token_table[i]->operator,"BYTE")==0){
		    int len = strlen_string(token_table[i]->operand[0]);
		    for(int m=0;m<len;++m)
			  count+= printf("%c",object_table[i].op[m]);
		}
		else if(token_table[i]->operator!=NULL && strcmp(token_table[i]->operator,"WORD")==0 && strcmp(token_table[i]->label,"MAXLEN")==0){
		    for(int m=0;m<3;++m)
			  count+= printf("%02X", object_table[i].op[m]);
		}


	  }
	  if(token_table[i]->operator!=NULL && strcmp(token_table[i]->operator,"RESW")==0 || strcmp(token_table[i]->operator,"RESB")==0)
		is_before_res=1;

	  if(i!=token_line-1){							// 다음 명령어의 object code의 길이를 구한다.
		if(token_table[i+1]->operator!=NULL &&token_table[i+1]->operator[0]=='=')
		    next_len=strlen_string(token_table[i+1]->operator);
		else if(token_table[i+1]->operator!=NULL && token_table[i+1]->operator[0]=='+')
		    next_len=4;
		else if(token_table[i+1]->operator!=NULL && strcmp(token_table[i+1]->operator,"BYTE")==0)
		    next_len=1;
		else if(token_table[i+1]->operator!=NULL && strcmp(token_table[i+1]->operator,"WORD")==0)
		    next_len=3;
		else if(token_table[i+1]->operator!=NULL && operator_format(token_table[i+1])==2)
		    next_len=2;
		else if(token_table[i+1]->operator!=NULL && operator_format(token_table[i+1])==3)
		    next_len=3;


		if(count+next_len*2>60){		//object 프로그램의 한 줄에서 명령어의 총 길이가 60을 넘을 경우
		    printf("\nT%06X%02X",address[i+1],count_object(i+1)/2);
		    count=0;
		}
		next_len=0;

	  }

	  if(strcmp(token_table[i]->operator,"CSECT")==0 || i == token_line-1){		// sect를 나눠야 할 경우
		is_before_res=0;
		printf("\n");
		for(int j=start_point;j<i;++j){			// Modification record가 필요한 부분을 찾는다.
		    for(int k=0;k<ref_len;++k)
			  if(token_table[j]->operand[0]!=NULL&& strcmp(token_table[j]->operand[0],extref[k])==0 && strcmp(token_table[j]->operator,"EXTREF")!=0)
				printf("M%06X05+%s\n",address[j]+1,extref[k]);

		    if(token_table[j]->label!=NULL && token_table[j]->operator!=NULL && strcmp(token_table[j]->label,"MAXLEN")==0 && strcmp(token_table[j]->operator,"EQU")!=0){	//MAXLEN 상대주소일 경우 예외 처리

			  for(int k=0;k<ref_len;++k)
				if(strcmp(maxlen_token[0],extref[k])==0)
				    printf("M%06X06+%s\n",address[j],extref[k]);
			  for(int k=0;k<ref_len;++k)
				if(strcmp(maxlen_token[1],extref[k])==0)
				    printf("M%06X06-%s\n",address[j],extref[k]);

		    }
		}
		printf("E%06X\n\n",address[start_point]);

		// 다음 sect의 header record ,define record, refer record, Text record를 저장한다.
		if(i!=token_line-1)
		    printf("H%-6s%06X",token_table[i]->label,address[i]);
		for(int j=i+1;j<token_line;++j){				// 해당 라인의 총 명령어 길이를 구한다.
		    if(token_table[j]->operator !=NULL && strcmp(token_table[j]->operator,"CSECT")==0){
			  printf("%06X\n",address[j-1]+3-address[i]);
			  break;
		    }
		    else if(j == token_line-1)
			  printf("%06X\n",address[j+1]-address[i]);
		}
		set_ext(i,fp);							//  extref,extdef 배열을 구한다.
		if(i!=token_line-1){
		    printf("T%06X%02X",address[start_point],count_object(i+1)/2);

		}
		count=next_len=0;
		start_point =i;



	  }

    }

}


