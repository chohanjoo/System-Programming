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


    for(int i=0;i<sym_line;++i)
	  printf("%s\t\t%X\n",sym_table[i].symbol,sym_table[i].addr);


    make_symtab_output("symtab_20160333");	// symbol ���̺��� ���Ͽ� �����մϴ�.

    if(assem_pass2() < 0 ){
	  printf(" assem_pass2: �н�2 �������� �����Ͽ����ϴ�.  \n") ; 
	  return -1 ; 
    }

    make_objectcode_output("output_20160333");		// objectcode ������ �����մϴ�. 


    memoryFree();		// �޸𸮸� �����մϴ�.

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
    toke->nixbpe=-1;
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

// symbol ���̺��� ����ü �迭�� �����ϴ� �Լ��̴�.
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

// symbol ���̺��� ���Ͽ� �����ϴ� �Լ��̴�.
void make_symtab_output(char *file_name){
    FILE *fp;

    fp=fopen(file_name,"w");

    for(int i=0;i<sym_line;++i)
	  fprintf(fp,"%s\t\t%d\n",sym_table[i].symbol,sym_table[i].addr);

    fclose(fp);


}

// �� ���� �� �ּҸ� �Ҵ��Ͽ� address �迭�� �����ϴ� �Լ��̴�.
int setting_line_address(){

    token *head = NULL;

    for(int i=0;i<token_line;++i)	// ��� ���� �ּҸ� -1�� �ʱ�ȭ ���ش�.
	  address[i]=-1;
    for(int i=0;i<token_line;++i){
	  head = token_table[i];

	  //�ش� ������ ��ɾ üũ�Ͽ� ���� �ּҰ��� �Ҵ��Ѵ�.
	  if(head->operator!=NULL && strcmp(head->operator,"START")==0)
		address[i]=0;
	  else if(head->operator !=NULL && strcmp(head->operator,"EXTDEF")==0 )
		set_def(head);									// extdef �迭�� ���� �ִ´�.
	  else if(head->operator!=NULL && strcmp(head->operator,"EXTREF")==0)
		set_ref(head);									// extref �迭�� ���� �ִ´�.
	  else if(head->label!=NULL && strcmp(head->label,"FIRST")==0){
		address[i]=address[0];
		address[i+1]=address[i] + operator_format(head);
	  }
	  else if(head->operator!=NULL && strcmp(head->operator,"RESW")==0)
		address[i+1] = address[i]+3*atoi(head->operand[0]);
	  else if(head->operator!=NULL && strcmp(head->operator,"RESB")==0)
		address[i+1] = address[i] + atoi(head->operand[0]);
	  else if(head->operator!=NULL&&strcmp(head->operator,"LTORG")==0){
		set_ltorg_next(i);								// literal���� LTORG �ڿ� �ִ´�.
	  }
	  else if(head->label!=NULL && strcmp(head->label,"*")==0){
		address[i+1] = address[i] + strlen_string(head->operator);		// literal�� ���̸� ���Ͽ� ���� ���� �ּҸ� ���Ѵ�.
	  }
	  else if(head->label!=NULL && strcmp(head->label,"MAXLEN")==0){	

		address[i] = maxlen_address(head,i);				// MAXLEN�� �ּҰ��� ���Ѵ�.
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

	  //���ͷ��� ������ �� ���ͷ� ���̺� ������ ���ͷ��� ���ͷ� ���̺� �־��ش�.
	  if(head->operand[0]!=NULL &&head->operand[0][0]=='='&& find_literal(head->operand[0])==-1){
		strcpy(literal_table[literal_line].symbol,head->operand[0]);
		literal_table[literal_line++].addr = -1;		// ���� �ּҸ� �𸣹Ƿ� -1���� �ʱ�ȭ�Ѵ�.
	  }
    }
}

// �ش� sect�� extdef �迭�� ���� �־��ش�.
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

//�ش� sect�� extref �迭�� ���� �־��ش�.
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

//LTORG ������ �� ���� ���Դ� ���ͷ��� token_table�� �־��ش�.
void set_ltorg_next(int i){
    for(int j=0;j<literal_line-not_use_literal;++j){	// ���ͷ� ���̺� �߿��� �������� ���� ���ͷ��� �ִٸ�
	  if(literal_line!=0 && not_use_literal==0){	//LTORG ������ ���� ù ���̺��϶�
		for(int j=token_line-1;j>i;--j)		// token_table�� �ϳ��� �̷��.
		    token_table[j+1]=token_table[j];
		input_literal(i);					//token_table�� ���ͷ��� �����Ѵ�.
		address[i+1]=address[i];
		literal_table[not_use_literal].addr = address[i+1];	// ���ͷ� ���̺� �ش� ������ �ּҰ��� �־��ش�.
		address[i]=-1;
		not_use_literal++;
	  }
	  else if(literal_line!=0){			// ù��° ���̺��� �ƴ� ��
		for(int j=token_line-1;j>i;--j)
		    token_table[j+1]=token_table[j];
		input_literal(i+not_use_literal);
		address[i+1] = address[i] + strlen_string(literal_table[not_use_literal].symbol);
		not_use_literal++;
	  }
    }

}

//end ������ ���̺��� �ִ� �Լ��̴�.
void set_end_next(int i){
    int temp=0;
    temp = literal_line-not_use_literal;
    for(int j=0;j<temp;++j){
	  if(literal_table[not_use_literal].symbol[1]=='X')		// ���̺��� 1byte �� ���
		locctr=1;
	  else if(literal_table[not_use_literal].symbol[1]=='C')
		locctr = strlen_string(literal_table[not_use_literal].symbol);

	  if(j==0){				//end ������ �ִ� ���̺��� ���
		input_literal(i);
		address[i+1] = address[i];
		address[i]=-1;
		address[i+2]=address[i+1] + locctr;
		literal_table[not_use_literal].addr = address[i+1];

	  }
	  else{				// end �� ù��° ���̺��� �ƴ� ���
		input_literal(i+j);
		address[i+2+j] = address[i+j+1] + locctr;
		literal_table[not_use_literal].addr = address[i+1+j];
	  }
	  not_use_literal++;
    }


}

// token_table�� ���̺��� �־��ش�.
void input_literal(int i){

    token_table[i+1]=(token*)malloc(sizeof(token));
    token_table[i+1]->label = (char*)malloc(sizeof(char)*2);
    strcpy(token_table[i+1]->label,"*");
    token_table[i+1]->operator = (char *)malloc(strlen(literal_table[not_use_literal].symbol)*sizeof(char));
    strcpy(token_table[i+1]->operator,literal_table[not_use_literal].symbol);

    // label �� operator �̿��� ��Ҵ� NULL �� �ʱ�ȭ�Ѵ�.
    for(int k=0;k<3;++k)
	  token_table[i+1]->operand[k]=NULL;
    token_table[i+1]->comment=NULL;
    token_line++;



}

// MAXLEN�� �ּҰ��� ���Ѵ�.
int maxlen_address(token *tk,int k){
    int result=0;
    char *pch;

    char temp[100]="";
    strcpy(temp,tk->operand[0]);
    pch = strtok(temp,"-");		// MAXLEN�� operand�� '-'�����ڸ� ����Ͽ� ��ū�� ������.
    maxlen_token[0] = (char*)malloc(sizeof(char)*strlen(pch));	//���� ��ū�� �������� maxlen_token �迭�� �ִ´�.
    strcpy(maxlen_token[0],pch);
    pch = strtok(NULL,"-");
    maxlen_token[1] = (char*)malloc(sizeof(char)*strlen(pch));
    strcpy(maxlen_token[1],pch);

    for(int i=0;i<ref_len;++i)		// maxlen�� �ش� sect���� ���ǵ��� ���� ���
	  for(int j=0;j<2;++j)
		if(strcmp(extref[0],maxlen_token[j])==0)
		    return address[k];
    for(int i=0;i<token_line;++i){		// maxlen�� �ش� sect���� ���ǵ� ���
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

//�ش� ���ͷ��� ���ͷ� ���̺� �ִ��� �˻��Ѵ�.
int find_literal(char *str){
    int result=-1;
    for(int i=0;i<literal_line;++i){
	  if(strcmp(literal_table[i].symbol,str)==0)
		result=i;		//���ͷ� ���̺� �ִٸ� index�� �����Ѵ�.

	  else
		result=-1;;
    }
    return result;
}

//�ش� ��ɾ��� ������ �����ϴ� �Լ��̴�.
int operator_format(token *tk){
    int result=0;
    for(int i=0;i<inst_index;++i)
	  if(strcmp(tk->operator,inst_table[i]->mnemonic)==0){
		if(strcmp(inst_table[i]->format,"3/4")==0)	// 3������ ���
		    result = 3;
		else if(tk->operator[0]=='+')				// 4������ ���
		    result=4;
		else
		    result = atoi(inst_table[i]->format);		// 2������ ���
		break;
	  }

    return result;

}

//���ͷ����� =' '�� ������ ���� ���� �����Ѵ�.
int strlen_string(char *str){
    for(int i=0;i<strlen(str);++i)
	  if(str[i]=='\''){
		++i;
		for(int j=0;j<10;++j){
		    string[j]=str[i++];		//�޸𸮿� �ø� ���ڸ� string �迭�� �����Ѵ�.
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

 setting_line_address();	// �� ���κ� �ּҸ� address �迭�� �Ҵ��մϴ�.

    setting_symbol_table();	// symbol ���̺��� ����ü �迭�� �����մϴ�.

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
    token *head=NULL;
    int k=0;
    for(int i=0;i<token_line;++i){
	  head= token_table[i];
	  object_table[i].op[0]=object_table[i].op[1]=object_table[i].op[2]='\0';

	  if((k=search_opcode(head->operator))!=-1){			// inst_table�� �ִ� ��ɾ��� ���
		object_table[i].opcode = inst_table[k]->opcods[0];	//object_table�� opcode�� �ش� ��ɾ��� opcode �� 4bit�� �ִ´�.

		head->nixbpe = inst_table[k]->opcods[1];			//token_table�� nixbpe�� �ش� ��ɾ��� opcode �� 4bit�� �ִ´�.

		if(operator_format(head)==2){		//��ɾ 2������ ���

		    set_nixbpe_format2(head);		// 2���� ��� nixbpe ����

		    for(int j=0;j<9;++j){		// ���������� ��ȣ�� op �迭�� �־��ش�.
			  if(strcmp(registers[j].name,head->operand[0])==0)
				object_table[i].op[1]=registers[j].number;
			  if(head->operand[1]!=NULL && strcmp(registers[j].name,head->operand[1])==0)
				object_table[i].op[2]=registers[j].number;
		    }


		    object_table[i].op[0]=head->nixbpe>>4;	// ��ɾ��� opcode �պκ� 4bit �� op�迭 0��° �濡 �����Ѵ�.	
		    object_table[i].op[0]&=0x0F;			// ��ȣ ��Ʈ�� 1�̸� ���� �̻��ϱ� ������ ����ũ ������ ���ش�.
		}
		else{						// ��ɾ 3,4������ ���

		    set_nixbpe(head);			// �ش� ��ɾ��� nixbpe�� �����Ѵ�.

		    object_table[i].op[0] = head->nixbpe >>4;		// nixbpe�� 4bit�� ��� op �迭�� �����Ͽ� ����ϱ� ���� ���·� �ٲ۴�.
		    object_table[i].op[0]&=0x0F;				// ��ȣ��Ʈ�� ���� ��츦 ����� ����ũ ������ ����Ѵ�.
		    object_table[i].op[1] = head->nixbpe & 0x0F;	// nixbpe�� 4bit�� ��� op �迭�� �����Ͽ� ����ϱ� ���� ���·� �ٲ۴�.

		    object_table[i].address = cal_address(head,i);	// target address ���� pc �� �� ���� object_table�� address�� �����Ѵ�.
		}



	  }
	  else if(head->label!=NULL &&strcmp(head->label,"*")==0){
		int len = strlen_string(head->operator);
		for(int m=0;m<len;++m)
		    object_table[i].op[m]=string[m];		// object_table �� op �迭�� �޸𸮿� �ø� ���ڿ��� �����Ѵ�.
	  }
	  else if(head->operator!=NULL &&strcmp(head->operator,"BYTE")==0){
		int len = strlen_string(head->operand[0]);
		head->nixbpe=0;
		for(int m=0;m<len;++m)
		    object_table[i].op[m]=string[m];		// object_table�� op �迭�� �޸𸮿� �ø� ���ڿ��� �����Ѵ�.
	  }
	  else if(head->operator!=NULL && strcmp(head->operator,"WORD")==0 && strcmp(head->label,"MAXLEN")==0){
		head->nixbpe=0;
		for(int m=0;m<3;++m)
		    object_table[i].op[m]=0;				// maxlen�� ����ּ��� ��� 0�� �����Ѵ�.
	  }



    }
    return 0;


}

// �ش� 3,4����  ��ɾ��� token_table�� nixbpe�� �������ִ� �Լ��̴�.
void set_nixbpe(token *tk){

    char bits=0;

    // nixbpe �� 2bit�� �������ش�.
    if(tk->nixbpe== '0')
	  tk->nixbpe=0x00;
    else if(tk->nixbpe == '4')
	  tk->nixbpe=0x01;
    else if(tk->nixbpe == '8')
	  tk->nixbpe=0x02;
    else if(tk->nixbpe=='C')
	  tk->nixbpe=0x03;

    tk->nixbpe<<=6;		// ����ũ ������ �̿��Ͽ� ���� ������ ������.


    //nibpe�� �� addressing mode�� �°� �������ش�.
    if(tk->operand[0]!=NULL && tk->operator!=NULL){
	  if(tk->operand[0][0]=='#')
		bits=0x10;
	  else if(tk->operand[0][0]=='@')
		bits=0x22;
	  else if(tk->operator[0]=='+')
		bits=0x31;
	  else
		bits=0x32;

	  tk->nixbpe |= bits;	// or �� ������ �̿��Ͽ� nixbpe�� �����Ѵ�.

	  //x bit�� �ش� ��ɾ �°� setting ���ش�.
	  if(tk->operand[1]!=NULL &&tk->operand[1][0]=='X')
		tk->nixbpe|=0x08;
	  else
		tk->nixbpe|=0x00;


    }
    else if(strcmp(tk->operator,"RSUB")==0)	//RSUB�� ��츸 ���� ����ó�� ���־���.
	  tk->nixbpe|=0x30;

}

// 2������ ��� nixbpe���� �� 2bit�� �����ϴ� �Լ��̴�.
void set_nixbpe_format2(token *tk){

    // nixbpe�� �� 2bit�� �����Ѵ�.
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

// target address - pc �� ������ִ� �Լ��̴�.
int  cal_address(token *tk,int index){
    int format=0;
    int i=0;
    int result=0;
    int maxlen_count=0;
    char *sym = tk->operand[0];
    i= operator_format(tk);

    if(tk->operand[0]==NULL)
	  return 0;

    if(i==3){					// ��ɾ 3������ ���

	  if(tk->operand[0][0]=='@')		//operand�� @�� ���� ��� �����͸� ��ĭ �̷� ��ɾ ���������� �񱳵� �� �ְ� �Ѵ�.
		sym+=1;

	  for(int j=0;j<sym_line;++j){
		if(strcmp(sym_table[j].symbol,sym)==0){
		    result = sym_table[j].addr - address[index+1];	// target address - pc �� ����Ѵ�.
		    if(strcmp(sym_table[j].symbol,"MAXLEN")==0 &&sym_table[j].addr+address[index]>4096)	// maxlen �����ּ��� ��� �Ѿ��.
			  continue;
		    if(result<0)				//������� ������ ��� ����ũ ���������ν� ������� ����ϴµ� ������ ���� �Ѵ�.
			  result&=0x00000FFF;
		    return result;
		}

	  }


	  for(int j=0;j<literal_line;++j)		// ��ɾ� table�� ���� literal_table�� ���� ���
		if(strcmp(literal_table[j].symbol,sym)==0){
		    result = literal_table[j].addr - address[index+1];	//�ּҰ��� ����Ѵ�.
		    return result;
		}

	  if(tk->operand[0][0]=='#'){			//immediate ������ ��� operand�� �ִ� ���ڸ� �����Ѵ�.
		result = atoi(tk->operand[0]+1);
		return result;
	  }

    }
    else if(i==4){			//��ɾ 4������ ���
	  for(int i=0;i<ref_len;++i)
		if(strcmp(extref[i],tk->operand[0])==0)
		    return 0;		// 0�� �����Ѵ�.

    }


    return result;
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

    FILE *fp;
    int i=0,k=0;
    int next_len=0,count=0;

    if((fp = fopen(file_name,"w"))==NULL){	//���� ���ڷ� NULL���� ���´ٸ� ǥ������Ѵ�.

	  printf("\n ���ڷ� NULL���� �������Ƿ� ǥ������մϴ�.\n");
	  error_print();
	  return ;
    }



    fprintf(fp,"HCOPY  ");					// ���α׷��� ù ����� ����Ѵ�.
    fprintf(fp,"%06X",address[0]);
    for(i=0;i<token_line;++i)
	  if(strcmp(token_table[i]->operator,"EQU")==0)
		break;


    fprintf(fp,"%06X\n",address[i]-address[0]);		// ���� ���α׷��� ���̸� ���Ѵ�.

    set_ext(0,fp);						// ���� ���α׷��� extref, extdef �迭�� ���� �ִ´�.

    fprintf(fp,"T%06X%02X",address[3],count_object(2)/2);	


    for(i=3;i<token_line;++i){
	  if(token_table[i]->nixbpe!=-1){					// �ش� ������ nixbpe�� ������ ���� ���� ���

		if(search_opcode(token_table[i]->operator)!=-1){	// ��ɾ inst_table�� ���� ���

		    if(operator_format(token_table[i])==2){		// ��ɾ 2������ ���

			  if(object_table[i].op[2]!='\0')			// �ǿ����ڰ� 2���� ��ɾ�
				count+= fprintf(fp,"%c%X%X%X",object_table[i].opcode,object_table[i].op[0],object_table[i].op[1],object_table[i].op[2]);
			  else							// �ǿ����ڰ� 1���� ��ɾ�
				count+= fprintf(fp,"%c%X%X0",object_table[i].opcode,object_table[i].op[0],object_table[i].op[1]);

		    }									// ��ɾ 3,4������ ���
		    else if(token_table[i]->nixbpe&0x01==0x01)
			  count+= fprintf(fp,"%c%X%X%05X",object_table[i].opcode,object_table[i].op[0],object_table[i].op[1],object_table[i].address);	//��ɾ 4������ ���
		    else
			  count+= fprintf(fp,"%c%X%X%03X",object_table[i].opcode,object_table[i].op[0],object_table[i].op[1],object_table[i].address);	//��ɾ 3������ ���
		}
		else if(token_table[i]->label!=NULL &&strcmp(token_table[i]->label,"*")==0){

		    if(is_before_res==1){								// ���ͷ� ���� resw �� resb�� ���� ��� ����
			  fprintf(fp,"\nT%06X%02X",address[i],count_object(i)/2);
			  is_before_res=0;
		    }
		    int len = strlen_string(token_table[i]->operator);
		    if(token_table[i]->operator!=NULL && token_table[i]->operator[1]=='X')	// 1 byte �� ���
			  count+=fprintf(fp,"%c%c",object_table[i].op[0],object_table[i].op[1]);
		    else if(token_table[i]->operator[1]=='C')						// 3 byte �� ���
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

	  if(i!=token_line-1){							// ���� ��ɾ��� object code�� ���̸� ���Ѵ�.
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


		if(count+next_len*2>60){		//object ���α׷��� �� �ٿ��� ��ɾ��� �� ���̰� 60�� ���� ���
		    fprintf(fp,"\nT%06X%02X",address[i+1],count_object(i+1)/2);
		    count=0;
		}
		next_len=0;

	  }

	  if(strcmp(token_table[i]->operator,"CSECT")==0 || i == token_line-1){		// sect�� ������ �� ���
		is_before_res=0;
		fprintf(fp,"\n");
		for(int j=start_point;j<i;++j){			// Modification record�� �ʿ��� �κ��� ã�´�.
		    for(int k=0;k<ref_len;++k)
			  if(token_table[j]->operand[0]!=NULL&& strcmp(token_table[j]->operand[0],extref[k])==0 && strcmp(token_table[j]->operator,"EXTREF")!=0)
				fprintf(fp,"M%06X05+%s\n",address[j]+1,extref[k]);

		    if(token_table[j]->label!=NULL && token_table[j]->operator!=NULL && strcmp(token_table[j]->label,"MAXLEN")==0 && strcmp(token_table[j]->operator,"EQU")!=0){	//MAXLEN ����ּ��� ��� ���� ó��

			  for(int k=0;k<ref_len;++k)
				if(strcmp(maxlen_token[0],extref[k])==0)
				    fprintf(fp,"M%06X06+%s\n",address[j],extref[k]);
			  for(int k=0;k<ref_len;++k)
				if(strcmp(maxlen_token[1],extref[k])==0)
				    fprintf(fp,"M%06X06-%s\n",address[j],extref[k]);

		    }
		}
		fprintf(fp,"E%06X\n\n",address[start_point]);

		// ���� sect�� header record ,define record, refer record, Text record�� �����Ѵ�.
		if(i!=token_line-1)
		    fprintf(fp,"H%-6s%06X",token_table[i]->label,address[i]);
		for(int j=i+1;j<token_line;++j){				// �ش� ������ �� ��ɾ� ���̸� ���Ѵ�.
		    if(token_table[j]->operator !=NULL && strcmp(token_table[j]->operator,"CSECT")==0){
			  fprintf(fp,"%06X\n",address[j-1]+3-address[i]);
			  break;
		    }
		    else if(j == token_line-1)
			  fprintf(fp,"%06X\n",address[j+1]-address[i]);
		}
		set_ext(i,fp);							//  extref,extdef �迭�� ���Ѵ�.
		if(i!=token_line-1){
		    fprintf(fp,"T%06X%02X",address[start_point],count_object(i+1)/2);

		}
		count=next_len=0;
		start_point =i;



	  }

    }


    fclose(fp);

}

//extdef, extref �迭�� ���� �־��ش�.
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


    if(def_len!=0){				//extdef �� ���� ��� ����Ѵ�.
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


    if(ref_len!=0){				//extref�� ���� ��� ����Ѵ�.
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


// Text record�� �� ���� ��ɾ� ���̸� ���ϴ� �Լ��̴�.
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


	  if(count+next_len>60){	// ���� ��ɾ� ���� ���� 60���� ũ�� �����Ѵ�.
		return count;
	  }
	  else if(token_table[i]->operator!=NULL && strcmp(token_table[i]->operator,"CSECT")==0)
		return count;
	  else if(token_table[i]->operator!=NULL && strcmp(token_table[i]->operator,"RESW")==0 || strcmp(token_table[i]->operator,"RESB")==0)	//resw,resb �̸� count ���� �����Ѵ�.
		return count;
	  else
		count+=next_len;

	  next_len=0;
    }

    return count;

}

// file_name�� NULL�� ���� �� ǥ��������ִ� �Լ��̴�.
void error_print(){
    int i=0,k=0;
    int next_len=0,count=0;
    FILE *fp=NULL;

    printf("HCOPY  ");					// ���α׷��� ù ����� ����Ѵ�.
    printf("%06X",address[0]);
    for(i=0;i<token_line;++i)
	  if(strcmp(token_table[i]->operator,"EQU")==0)
		break;


    printf("%06X\n",address[i]-address[0]);		// ���� ���α׷��� ���̸� ���Ѵ�.

    set_ext(0,fp);						// ���� ���α׷��� extref, extdef �迭�� ���� �ִ´�.

    printf("T%06X%02X",address[3],count_object(2)/2);	


    for(i=3;i<token_line;++i){
	  if(token_table[i]->nixbpe!=-1){					// �ش� ������ nixbpe�� ������ ���� ���� ���

		if(search_opcode(token_table[i]->operator)!=-1){	// ��ɾ inst_table�� ���� ���

		    if(operator_format(token_table[i])==2){		// ��ɾ 2������ ���

			  if(object_table[i].op[2]!='\0')			// �ǿ����ڰ� 2���� ��ɾ�
				count+= printf("%c%X%X%X",object_table[i].opcode,object_table[i].op[0],object_table[i].op[1],object_table[i].op[2]);
			  else							// �ǿ����ڰ� 1���� ��ɾ�
				count+= printf("%c%X%X0",object_table[i].opcode,object_table[i].op[0],object_table[i].op[1]);

		    }									// ��ɾ 3,4������ ���
		    else if(token_table[i]->nixbpe&0x01==0x01)
			  count+= printf("%c%X%X%05X",object_table[i].opcode,object_table[i].op[0],object_table[i].op[1],object_table[i].address);	//��ɾ 4������ ���
		    else
			  count+= printf("%c%X%X%03X",object_table[i].opcode,object_table[i].op[0],object_table[i].op[1],object_table[i].address);	//��ɾ 3������ ���
		}
		else if(token_table[i]->label!=NULL &&strcmp(token_table[i]->label,"*")==0){

		    if(is_before_res==1){								// ���ͷ� ���� resw �� resb�� ���� ��� ����
			  printf("\nT%06X%02X",address[i],count_object(i)/2);
			  is_before_res=0;
		    }
		    int len = strlen_string(token_table[i]->operator);
		    if(token_table[i]->operator!=NULL && token_table[i]->operator[1]=='X')	// 1 byte �� ���
			  count+=printf("%c%c",object_table[i].op[0],object_table[i].op[1]);
		    else if(token_table[i]->operator[1]=='C')						// 3 byte �� ���
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

	  if(i!=token_line-1){							// ���� ��ɾ��� object code�� ���̸� ���Ѵ�.
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


		if(count+next_len*2>60){		//object ���α׷��� �� �ٿ��� ��ɾ��� �� ���̰� 60�� ���� ���
		    printf("\nT%06X%02X",address[i+1],count_object(i+1)/2);
		    count=0;
		}
		next_len=0;

	  }

	  if(strcmp(token_table[i]->operator,"CSECT")==0 || i == token_line-1){		// sect�� ������ �� ���
		is_before_res=0;
		printf("\n");
		for(int j=start_point;j<i;++j){			// Modification record�� �ʿ��� �κ��� ã�´�.
		    for(int k=0;k<ref_len;++k)
			  if(token_table[j]->operand[0]!=NULL&& strcmp(token_table[j]->operand[0],extref[k])==0 && strcmp(token_table[j]->operator,"EXTREF")!=0)
				printf("M%06X05+%s\n",address[j]+1,extref[k]);

		    if(token_table[j]->label!=NULL && token_table[j]->operator!=NULL && strcmp(token_table[j]->label,"MAXLEN")==0 && strcmp(token_table[j]->operator,"EQU")!=0){	//MAXLEN ����ּ��� ��� ���� ó��

			  for(int k=0;k<ref_len;++k)
				if(strcmp(maxlen_token[0],extref[k])==0)
				    printf("M%06X06+%s\n",address[j],extref[k]);
			  for(int k=0;k<ref_len;++k)
				if(strcmp(maxlen_token[1],extref[k])==0)
				    printf("M%06X06-%s\n",address[j],extref[k]);

		    }
		}
		printf("E%06X\n\n",address[start_point]);

		// ���� sect�� header record ,define record, refer record, Text record�� �����Ѵ�.
		if(i!=token_line-1)
		    printf("H%-6s%06X",token_table[i]->label,address[i]);
		for(int j=i+1;j<token_line;++j){				// �ش� ������ �� ��ɾ� ���̸� ���Ѵ�.
		    if(token_table[j]->operator !=NULL && strcmp(token_table[j]->operator,"CSECT")==0){
			  printf("%06X\n",address[j-1]+3-address[i]);
			  break;
		    }
		    else if(j == token_line-1)
			  printf("%06X\n",address[j+1]-address[i]);
		}
		set_ext(i,fp);							//  extref,extdef �迭�� ���Ѵ�.
		if(i!=token_line-1){
		    printf("T%06X%02X",address[start_point],count_object(i+1)/2);

		}
		count=next_len=0;
		start_point =i;



	  }

    }

}


