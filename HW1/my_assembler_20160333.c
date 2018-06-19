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

    make_opcode_output("output_20160333");


    memoryFree();
    /*
     * 추후 프로젝트에서 사용되는 부분
     *
     if(assem_pass2() < 0 ){
     printf(" assem_pass2: 패스2 과정에서 실패하였습니다.  \n") ; 
     return -1 ; 
     }

     make_objectcode_output("output") ; 
     */
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

}
