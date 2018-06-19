/*
 * ȭ�ϸ� : my_assembler_00000000.c 
 * ��  �� : �� ���α׷��� SIC/XE �ӽ��� ���� ������ Assembler ���α׷��� ���η�ƾ����,
 * �Էµ� ������ �ڵ� ��, ��ɾ �ش��ϴ� OPCODE�� ã�� ����Ѵ�.
 * ���� ������ ���Ǵ� ���ڿ� "00000000"���� �ڽ��� �й��� �����Ѵ�.
 */

/*
 *
 * ���α׷��� ����� �����Ѵ�. 
 *
 */

#include <stdio.h>	
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>
#include <stdlib.h>

// ���ϸ��� "00000000"�� �ڽ��� �й����� ������ ��.
#include "my_assembler_20160333.h"

/* ----------------------------------------------------------------------------------
 * ���� : ����ڷ� ���� ����� ������ �޾Ƽ� ��ɾ��� OPCODE�� ã�� ����Ѵ�.
 * �Ű� : ���� ����, ����� ���� 
 * ��ȯ : ���� = 0, ���� = < 0 
 * ���� : ���� ����� ���α׷��� ����Ʈ ������ �����ϴ� ��ƾ�� ������ �ʾҴ�. 
 *		   ���� �߰������� �������� �ʴ´�. 
 * ----------------------------------------------------------------------------------
 */
int main(int args, char *arg[]) 
{
    if(init_my_assembler()< 0)
    {
	  printf("init_my_assembler: ���α׷� �ʱ�ȭ�� ���� �߽��ϴ�.\n"); 
	  return -1 ; 
    }

    if(assem_pass1() < 0 ){
	  printf("assem_pass1: �н�1 �������� �����Ͽ����ϴ�.  \n") ; 
	  return -1 ; 
    }

    make_opcode_output("output_20160333");


    memoryFree();
    /*
     * ���� ������Ʈ���� ���Ǵ� �κ�
     *
     if(assem_pass2() < 0 ){
     printf(" assem_pass2: �н�2 �������� �����Ͽ����ϴ�.  \n") ; 
     return -1 ; 
     }

     make_objectcode_output("output") ; 
     */
    return 0;
}

// �޸� �Ҵ��� ���������ִ� �Լ��̴�.
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
 * ���� : ���α׷� �ʱ�ȭ�� ���� �ڷᱸ�� ���� �� ������ �д� �Լ��̴�. 
 * �Ű� : ����
 * ��ȯ : �������� = 0 , ���� �߻� = -1
 * ���� : ������ ��ɾ� ���̺��� ���ο� �������� �ʰ� ������ �����ϰ� �ϱ� 
 *		   ���ؼ� ���� ������ �����Ͽ� ���α׷� �ʱ�ȭ�� ���� ������ �о� �� �� �ֵ���
 *		   �����Ͽ���. 
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
 * ���� : �ӽ��� ���� ��� �ڵ��� ������ �о� ���� ��� ���̺�(inst_table)�� 
 *        �����ϴ� �Լ��̴�. 
 * �Ű� : ���� ��� ����
 * ��ȯ : �������� = 0 , ���� < 0 
 * ���� : ���� ������� ������ �����Ӱ� �����Ѵ�. ���ô� ������ ����.
 *	
 *	===============================================================================
 *		   | �̸� | ���� | ���� �ڵ� | ���۷����� ���� | NULL|
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
	  // inst.data ���� �� �� �� �Է¹޾� temp �迭�� �����Ѵ�.
	  fgets(temp,sizeof(temp),file);	
	  if(feof(file))
		break;
	  //inst_table�迭 ���� index 0 ���� �����Ҵ� ���ش�.
	  inst_table[i]=(inst *)malloc(sizeof(inst));	
	  // �о�� instruction�� strcpy �Լ��� �̿��Ͽ� ��ū�� ���� �����Ѵ�.

	  pch = strtok(temp,"\t ");
	  // mnemonic ������ �����Ҵ��Ͽ� ��ū�� �����Ѵ�.
	  inst_table[i]->mnemonic = (char *)malloc(strlen(pch));
	  strcpy(inst_table[i]->mnemonic,pch);

	  // countOperand ������ �����Ҵ��Ͽ� ��ū�� �����Ѵ�.
	  pch = strtok(NULL,"\t ");
	  inst_table[i]->countOperand =(char *)malloc(strlen(pch));
	  strcpy(inst_table[i]->countOperand,pch);

	  // format ������ �����Ҵ��Ͽ� ��ū�� �����Ѵ�.
	  pch = strtok(NULL,"\t ");
	  inst_table[i]->format = (char *)malloc(strlen(pch));
	  strcpy(inst_table[i]->format,pch);

	  // opcods ������ �����Ҵ��Ͽ� ��ū�� �����Ѵ�.
	  // opcods �� ������ ��ū�̹Ƿ� �������� '\0'���� �־��� ������ ���ش�.
	  pch = strtok(NULL,"\t ");
	  pch[strlen(pch)-2]='\0';
	  inst_table[i]->opcods =(char *)malloc(strlen( pch));
	  strcpy(inst_table[i]->opcods,pch);

	  inst_index++;


    }

    return errno;
}

/* ----------------------------------------------------------------------------------
 * ���� : ����� �� �ҽ��ڵ带 �о� �ҽ��ڵ� ���̺�(input_data)�� �����ϴ� �Լ��̴�. 
 * �Ű� : ������� �ҽ����ϸ�
 * ��ȯ : �������� = 0 , ���� < 0  
 * ���� : ���δ����� �����Ѵ�.
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
	  // input_file ���� ���پ� �Է¹޾� input_data �迭�� �־��ش�.
	  fgets(temp,sizeof(temp),file);
	  if(feof(file))
		break;
	  // input_data �迭�� ������� �ҽ��ڵ带 �����ϱ� ���� �����Ҵ��Ѵ�.
	  input_data[i]=(char *)malloc(strlen(temp));
	  strcpy(input_data[i],temp);
	  line_num++;
    }
    return errno;
}

/* ----------------------------------------------------------------------------------
 * ���� : �ҽ� �ڵ带 �о�� ��ū������ �м��ϰ� ��ū ���̺��� �ۼ��ϴ� �Լ��̴�. 
 *        �н� 1�� ���� ȣ��ȴ�. 
 * �Ű� : �Ľ��� ���ϴ� ���ڿ�  
 * ��ȯ : �������� = 0 , ���� < 0 
 * ���� : my_assembler ���α׷������� ���δ����� ��ū �� ������Ʈ ������ �ϰ� �ִ�. 
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

    // temp_str �迭�� str ���ڿ��� �����Ͽ� str�� ������ �ڿ� �ٽ� ������� �ǵ�����.
    strcpy(temp_str,str);
    init_token(token_table[token_line]);

    if(str==NULL)
	  return -1;

    if(str[0]=='.'){		 //.���� �����ϴ� �ּ� �����ϰ� �����Ҵ� �����Ѵ�.
	  free(token_table[token_line]);
	  return 0;
    }

    if(str[0]=='\t'||str[0]==' '){		  //label�� ���� �� NULL �����Ѵ�.
	  token_table[token_line]->label =NULL;
	  // pch �� operator �����Ѵ�.
	  pch = strtok(str,"\t ");
    }
    else{		  //label�� ���� ��
	  //pch ��  label token ����
	  pch = strtok(str,"\t ");			
	  token_table[token_line]->label =(char *)malloc(strlen(pch));
	  strcpy(token_table[token_line]->label,pch);
	  //label ���� ������ ������� ���๮�ڸ� ���ְ� �Լ� �����Ѵ�.
	  if(token_table[token_line]->label[strlen(pch)-1]=='\n'){	
		token_table[token_line++]->label[strlen(pch)-2]='\0';
		//������ str�� �������·� �ǵ�����.
		strcpy(str,temp_str);
		return 0;
	  }
	  //pch�� operator �����Ѵ�.
	  pch = strtok(NULL, "\t ");
    }

    //operator�� �����Ҵ� �Ͽ� ��ū�� �����Ѵ�.
    token_table[token_line]->operator =(char *)malloc(strlen(pch));
    strcpy(token_table[token_line]->operator,pch);
    //���� operator�� ���๮�ڰ� ���� ��� '\0'�� �ٲ��ش�.
    if(token_table[token_line]->operator[strlen(pch)-1]=='\n'){
	  token_table[token_line++]->operator[strlen(pch)-2]='\0';
	  //������ str�� �������·� �ǵ�����.
	  strcpy(str,temp_str);
	  return 0;
    }

    // RSUB ��ɾ�� operand �� ���⶧���� ���� ����ó�� ���־���.
    if(strcmp(pch,"RSUB")==0){
	  //pch�� comment�� �����Ѵ�.
	  pch = strtok(NULL,"\t ");		
	  pch[strlen(pch)-2]='\0';
	  //comment ������ �����Ҵ� ���ְ� ��ū �����Ͽ� �Լ� �����Ѵ�.
	  token_table[token_line]->comment = (char *)malloc(strlen(pch));
	  strcpy(token_table[token_line]->comment,pch);
	  token_line++;
	  //������ str�� �������·� �ǵ�����.
	  strcpy(str,temp_str);
	  return 0;
    }

    // pch �� operator �����Ѵ�.
    pch = strtok(NULL,"\t ");

    //operand�� buffer �� �����Ѵ�.
    buffer = (char*)malloc(strlen(pch));
    strcpy(buffer,pch);

    if(pch[strlen(pch)-1]!='\n'){		//comment �� ���� ���
	  //pch �� comment �����Ѵ�.
	  pch = strtok(NULL,"\t ");

	  //comment �� �����Ҵ� ���� �� '\n' �ڸ��� '\0'�� ��ü�Ѵ�.
	  token_table[token_line]->comment = (char *)malloc(strlen(pch));
	  strcpy(token_table[token_line]->comment,pch);
	  token_table[token_line]->comment[strlen(pch)-2]='\0';
    }

    //operand ��  ������ ','�� �Ͽ� ��ūȭ�Ѵ�.
    temp = strtok(buffer,",");
    while(temp!=NULL){
	  //������ ��ū�̸� '\n'�� �����ش�.
	  if(temp[strlen(temp)-1]=='\n')
		temp[strlen(temp)-2]='\0';
	  token_table[token_line]->operand[i] = (char *)malloc(strlen(temp));
	  strcpy(token_table[token_line]->operand[i++],temp);
	  temp = strtok(NULL,",");
    }

    token_line++;
    //������ str�� �������·� �ǵ�����.
    strcpy(str,temp_str);
    return 0;


}

void init_token(token *toke){		//inst_unit ����ü�� �ʱ�ȭ ���ִ� �Լ��̴�.
    toke->label = NULL;
    toke->operator = NULL;
    toke->comment = NULL;
    for(int i=0;i<3;++i)
	  toke->operand[i]=NULL;
}


/* ----------------------------------------------------------------------------------
 * ���� : �Է� ���ڿ��� ���� �ڵ������� �˻��ϴ� �Լ��̴�. 
 * �Ű� : ��ū ������ ���е� ���ڿ� 
 * ��ȯ : �������� = ���� ���̺� �ε���, ���� < 0 
 * ���� : 
 *		
 * ----------------------------------------------------------------------------------
 */
int search_opcode(char *str) 
{
    /* add your code here */

    char temp[20];
    strcpy(temp,str);

    //�Է¹��ڿ��� ù��° ���ڰ� '+'�̸� temp �迭�� �ϳ��� ������ '+'���ڸ� �����ش�.
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
 * ���� : ����� �ڵ带 ���� �н�1������ �����ϴ� �Լ��̴�.
 *		   �н�1������..
 *		   1. ���α׷� �ҽ��� ��ĵ�Ͽ� �ش��ϴ� ��ū������ �и��Ͽ� ���α׷� ���κ� ��ū
 *		   ���̺��� �����Ѵ�.
 *
 * �Ű� : ����
 * ��ȯ : ���� ���� = 0 , ���� = < 0
 * ���� : ���� �ʱ� ���������� ������ ���� �˻縦 ���� �ʰ� �Ѿ �����̴�.
 *	  ���� ������ ���� �˻� ��ƾ�� �߰��ؾ� �Ѵ�.
 *
 * -----------------------------------------------------------------------------------
 */
static int assem_pass1(void)
{
    /* add your code here */

    /* input_data�� ���ڿ��� ���پ� �Է� �޾Ƽ� 
     * token_parsing()�� ȣ���Ͽ� token_unit�� ����
     */
    for(int i=0;i<line_num;++i)
	  if(token_parsing(input_data[i])==-1)
		return -1;


    return 0;




}


/* ----------------------------------------------------------------------------------
 * ���� : �Էµ� ���ڿ��� �̸��� ���� ���Ͽ� ���α׷��� ����� �����ϴ� �Լ��̴�.
 *        ���⼭ ��µǴ� ������ ��ɾ� ���� OPCODE�� ��ϵ� ǥ(���� 4��) �̴�.
 * �Ű� : ������ ������Ʈ ���ϸ�
 * ��ȯ : ����
 * ���� : ���� ���ڷ� NULL���� ���´ٸ� ���α׷��� ����� ǥ��������� ������
 *        ȭ�鿡 ������ش�.
 *        ���� ���� 4�������� ���̴� �Լ��̹Ƿ� ������ ������Ʈ������ ������ �ʴ´�.
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
	  printf("������ ���� ���Ͽ����ϴ�.\n");
	  return ;
    }

    // token_line��ŭ token_table �迭 �� �渶���� �� ��ҵ��� output ���Ͽ� ����Ѵ�.
    for(int i=0;i<token_line;++i){
	  head = token_table[i];
	  //label�� ������ �Ǹ� ����Ѵ�.
	  if(head->label==NULL)
		fprintf(file,"\t");
	  else
		fprintf(file,"%s\t",head->label);

	  //operator�� ������ �Ǹ� ����Ѵ�.
	  if(head->operator==NULL)
		fprintf(file,"\t");
	  else
		fprintf(file,"%s\t",head->operator);

	  // operator ����Ѵ�.
	  for(int j=0;j<3;++j){
		//operand�� ���̻� ������ �� ����Ѵ�.
		if(head->operand[j]==NULL){
		    fprintf(file,"\t");
		    break;
		}
		else{
		    if(j==0)	// ó������ϴ� operand
			  fprintf(file,"%s",head->operand[j]);
		    else		// �ι�°���ʹ�  operand�տ� ','����Ѵ�.
			  fprintf(file,",%s",head->operand[j]);
		}
	  }


	  k = search_opcode(head->operator);
	  if(k!=-1)
		fprintf(file,"%s\n",inst_table[k]->opcods);
	  else	 //��ɾ �ƴ� ��� ������ ����Ѵ�.
		fprintf(file,"\n");
    }

}



/* --------------------------------------------------------------------------------*
 * ------------------------- ���� ������Ʈ���� ����� �Լ� --------------------------*
 * --------------------------------------------------------------------------------*/


/* ----------------------------------------------------------------------------------
 * ���� : ����� �ڵ带 ���� �ڵ�� �ٲٱ� ���� �н�2 ������ �����ϴ� �Լ��̴�.
 *		   �н� 2������ ���α׷��� ����� �ٲٴ� �۾��� ���� ������ ����ȴ�.
 *		   ������ ���� �۾��� ����Ǿ� ����.
 *		   1. ������ �ش� ����� ��ɾ ����� �ٲٴ� �۾��� �����Ѵ�.
 * �Ű� : ����
 * ��ȯ : �������� = 0, �����߻� = < 0
 * ���� :
 * -----------------------------------------------------------------------------------
 */
static int assem_pass2(void)
{

    /* add your code here */

}

/* ----------------------------------------------------------------------------------
 * ���� : �Էµ� ���ڿ��� �̸��� ���� ���Ͽ� ���α׷��� ����� �����ϴ� �Լ��̴�.
 *        ���⼭ ��µǴ� ������ object code (������Ʈ 1��) �̴�.
 * �Ű� : ������ ������Ʈ ���ϸ�
 * ��ȯ : ����
 * ���� : ���� ���ڷ� NULL���� ���´ٸ� ���α׷��� ����� ǥ��������� ������
 *        ȭ�鿡 ������ش�.
 *
 * -----------------------------------------------------------------------------------
 */
void make_objectcode_output(char *file_name)
{
    /* add your code here */

}
