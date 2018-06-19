/* 
 * my_assembler 함수를 위한 변수 선언 및 매크로를 담고 있는 헤더 파일이다. 
 * 
 */
#define MAX_INST 256
#define MAX_LINES 5000
#define MAX_OPERAND 3

/* 
 * instruction 목록 파일로 부터 정보를 받아와서 생성하는 구조체 변수이다.
 * 구조는 각자의 instruction set의 양식에 맞춰 직접 구현하되
 * 라인 별로 하나의 instruction을 저장한다.
 */
struct inst_unit {
	/* add your code here */
	char *mnemonic;
	char * countOperand;
	char *format;
	char *opcods;

};
typedef struct inst_unit inst;
inst *inst_table[MAX_INST];
int inst_index=0;

/*
 * 어셈블리 할 소스코드를 입력받는 테이블이다. 라인 단위로 관리할 수 있다.
 */
char *input_data[MAX_LINES];
static int line_num;

int label_num; 

/* 
 * 어셈블리 할 소스코드를 토큰단위로 관리하기 위한 구조체 변수이다.
 * operator는 renaming을 허용한다.
 * nixbpe는 8bit 중 하위 6개의 bit를 이용하여 n,i,x,b,p,e를 표시한다.
 */
struct token_unit {
	char *label;
	char *operator; 
	char *operand[MAX_OPERAND];
	char *comment;
	char nixbpe; // 추후 프로젝트에서 사용된다.

};

typedef struct token_unit token; 
token *token_table[MAX_LINES]; 
static int token_line;

/*
 * 심볼을 관리하는 구조체이다.
 * 심볼 테이블은 심볼 이름, 심볼의 위치로 구성된다.
 */
struct symbol_unit {
	char symbol[10];
	int addr;
};

typedef struct symbol_unit symbol;
symbol sym_table[MAX_LINES];
static int sym_line;

int address[MAX_LINES];		//코드 각 라인 별 주소값 배열

char string[10];

typedef struct literal_unit{
    char symbol[10];
    int addr;
}literal;

literal literal_table[10];
static int literal_line;
int not_use_literal;

char *extdef[3];
char *extref[3];
int def_len;
int ref_len;

static int locctr;

typedef struct {
    char opcode;
    char op[3];
    short address;
}object;
object object_table[MAX_LINES];

typedef struct{
    char name[3];
    int  number;
}reg;
reg registers[9]={{"A",0},{"X",1},{"L",2},{"B",3},{"S",4},{"T",5},{"F",6},{"PC",8},{"SW",9}};


int start_point;
char *maxlen_token[2];

int is_before_res;
//--------------

static char *input_file;
static char *output_file;
int init_my_assembler(void);
int init_inst_file(char *inst_file);
int init_input_file(char *input_file);
int token_parsing(char *str);
int search_opcode(char *str);
static int assem_pass1(void);
void make_opcode_output(char *file_name);

void make_symtab_output(char *);
void init_token(token*);
void memoryFree(void);
int strlen_string(char *);
int operator_format(token *);
int setting_line_address();
int setting_symbol_table();
int find_literal(char *str);
void input_literal(int i);
int maxlen_address(token *tk,int);
void set_def(token *tk);
void set_ref(token *tk);
void set_ltorg_next(int i);
void set_end_next(int i);
void set_nixbpe(token *);
int cal_address(token *tk,int);
void set_nixbpe_format2(token *tk);
void set_ext(int index,FILE*);
int count_object(int index);
void error_print();

/* 추후 프로젝트에서 사용하게 되는 함수*/
static int assem_pass2(void);
void make_objectcode_output(char *file_name);
