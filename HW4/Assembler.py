'''
 Assembler : 이 프로그램은 SIC/XE 머신을 위한 Assembler 프로그램의 메인 루틴이다. 프로그램의 수행 작업은 다음과
 같다. <br>
 1) 처음 시작하면 Instruction 명세를 읽어들여서 assembler를 세팅한다. <br>
 2) 사용자가 작성한 input 파일을 읽어들인 후 저장한다. <br>
 3) input 파일의 문장들을 단어별로 분할하고 의미를 파악해서 정리한다. (pass1) <br>
 4) 분석된 내용을 바탕으로 컴퓨터가 사용할 수 있는 object code를 생성한다. (pass2) <br>

 <br>
 <br>
 작성중의 유의사항 : <br>
 1) 새로운 클래스, 새로운 변수, 새로운 함수 선언은 얼마든지 허용됨. 단, 기존의 변수와 함수들을 삭제하거나 완전히 대체하는 것은
 안된다.<br>
 2) 마찬가지로 작성된 코드를 삭제하지 않으면 필요에 따라 예외처리, 인터페이스 또는 상속 사용 또한 허용됨.<br>
 3) 모든 void 타입의 리턴값은 유저의 필요에 따라 다른 리턴 타입으로 변경 가능.<br>
 4) 파일, 또는 콘솔창에 한글을 출력시키지 말 것. (채점상의 이유. 주석에 포함된 한글은 상관 없음)<br>

 <br>
 <br>
 + 제공하는 프로그램 구조의 개선방법을 제안하고 싶은 분들은 보고서의 결론 뒷부분에 첨부 바랍니다. 내용에 따라 가산점이 있을 수
 있습니다.

'''
from SymbolTable import SymbolTable

from InstTable import InstTable
from TokenTable import TokenTable, Token


class Assembler:
    instTable = ""  #instruction 명세를 저장한 공간
    lineList = []   #읽어들인 input 파일의 내용을 한 줄 씩 저장하는 공간.
    symtabList = []     #프로그램의 section별로 symbol table을 저장하는 공간
    TokenList = []      # 프로그램의 section별로 프로그램을 저장하는 공간
    codList = []        #Token, 또는 지시어에 따라 만들어진 오브젝트 코드들을 출력 형태로 저장하는 공간.
    sec_codeList = []   #codeList를 sect별로 관리한다.

    locator = 0

    def __init__(self,instFile):
        self.instTable = InstTable(instFile)
        self.locator = 0
        self.codList = []

    def loadInputFile(self,inputFile):      #inputFile을 읽어들여서 lineList에 저장한다.
        file = open(inputFile, 'r')
        while True:
            line = file.readline()
            if not line: break
            self.lineList.append(line)
        file.close()

#1) 프로그램 소스를 스캔하여 토큰단위로 분리한 뒤 토큰테이블 생성
#2) label을 symbolTable에 정리
    def pass1(self):

        symTable = SymbolTable()
        tokenTable = TokenTable(self.instTable)
        tk = ""
        pre_sect_index = 0


        for i in range(len(self.lineList)):     #linelist의 한 요소씩 접근한다.
            line = self.lineList[i]
            token = Token(line)

            if token.operator!="" and token.operator in ("END", "CSECT"):   # operator가 END 또는 CSECT면
                if token.operator == "END":     #END일 때 현재 라인을 토큰화하여 저장한다.
                    tokenTable.putToken(line)
                    self.instLoaction(tokenTable, token, symTable)


                self.symtabList.append(symTable)    #현재까지의 symTable을 symtabList에 저장한다.
                tokenTable.setSymTable(symTable)    #현재의 tokenTable에 symtable을 넣는다.
                self.TokenList.append(tokenTable)   #TokenList에 현재의 toknenTable을 저장한다.

                del symTable
                symTable = SymbolTable()            # symtable을 초기화한다.

                tokenTable = TokenTable(self.instTable)     #새로운 TokenTable을 생성한다.
                self.locator = 0

                symTable.putSymbol(token.label,0)       #새로 만든 symTable에 현재의 라벨을 저장
                if token.operator == "CSECT":
                    tokenTable.putToken(line)

            else:                               #operator가 END , CSECT 이외의 token일 경우
                tokenTable.putToken(line)       #tokenTable에 명령어를 넣어 토큰화한다.
                if token.label!="" and token.label != '.':      #label이 있으면 symtable에 넣는다.
                    symTable.putSymbol(token.label,0)

                if len(token.operand)!=0 and token.operator != "RSUB" and token.operand[0][0] == '=':   #literal일 경우 literalTable에 저장한다.
                        tokenTable.literTab.putLiteral(token.operand[0],0)

                if token.operator!="" and token.operator=="EXTDEF":         #EXTDEF일 경우 해당 tokenTable의 extdef 리스트에 저장한다.
                    for k in range(len(token.operand)):
                        tokenTable.putExtdef(token.operand[k])

                elif token.operator!="" and token.operator == "EXTREF":     #EXTREF일 경우 해당 tokenTable의 extref 리스트에 저장한다
                    for k in range(len(token.operand)):
                        tokenTable.putExtref(token.operand[k])

                self.instLoaction(tokenTable,token,symTable)            #해당 명령어의 location값을 계산한다.

        self.outputSymbolTable("symtab_20160333")       #symtable을 파일에 저장한다.



    def instLoaction(self,tokenTable,token,symbolTable):        #해당 명령어의 location을 계산한다.
        literalTable = tokenTable.literTab
        if token.operator != "" and token.operator== "LTORG" or token.operator != "" and token.operator=="END":
            tokenTable.setLocation(0)

            if token.operator=="LTORG":         #LTORG를 만났을 경우 flag를 설정한다.
                literalTable.ltorg_flag = True

            for key in literalTable.literalList.keys():
                literalTable.literalList[key] = self.locator        #리터럴의 주소를 재설정한다.
                self.locator += len(literalTable.getLiteral(key))   #다음 명령어의 주소를 저장한다.

        elif token.operator!="" and token.operator == "EQU":    # operator 가 EQU 인 경우
            result = 0
            index = 0

            if token.operand[0] != '*':                     # MAXLEN의 절대주소값을 계산한다.
                word = token.operand[0].split('-')
                index = symbolTable.symbolList.index(word[0])
                result += symbolTable.locationList[index]
                index = symbolTable.symbolList.index(word[1])
                result -= symbolTable.locationList[index]

                tokenTable.setLocation(result)

            else:
                tokenTable.setLocation(self.locator)

        else:
            tokenTable.setLocation(self.locator)

        if token.label != "" and token.label != '.' :
            index = symbolTable.symbolList.index(token.label)
            token_2 = tokenTable.tokenList[len(tokenTable.tokenList)-1]
            symbolTable.locationList[index] = token_2.location

        if token.operator !="":
            if self.instTable.search(token.operator) :      #명령어가 instTable에 있는 경우
                if token.operator[0]=='+':                  #4형식 명령어인 경우 명령어 검색을 위해 명령어에서 '+'를 제외한다.
                    inst = self.instTable.instMap[token.operator[1:]]
                else:
                    inst = self.instTable.instMap[token.operator]

                if token.operator[0] == '+':    #4형식 명령어인 경우
                    self.locator += 4
                elif inst.format == 3:          #3형식 명령어인 경우
                    self.locator += 3
                elif inst.format == 2:          #2형식 명령어인 경우
                    self.locator += 2

            elif token.operator == "WORD" :
                self.locator += 3
            elif token.operator == "RESW":
                self.locator += 3 * int(token.operand[0])
            elif token.operator == "RESB":
                self.locator += int(token.operand[0])
            elif token.operator=="BYTE" :
                if token.operand[0][0] == 'X':
                    self.locator+=1

# 1) 분석된 내용을 바탕으로 object code를 생성하여 codeList에 저장.
    def pass2(self):
        for i in range(len(self.TokenList)):                #tokentable을 sect별로 접근한다.
            for j in range(len(self.TokenList[i].tokenList)):
                objectcode = self.TokenList[i].makeObjectCode(j)

                if objectcode != "":
                    self.codList.append(objectcode)     #codeList에 objectcode를 저장한다.

            litertable = self.TokenList[i].literTab

            if len(litertable.literalList)!=0 and litertable.ltorg_flag == False :      #objectcode 다음에 resw 나 resb이 없는 리터럴인 경우 codelist에 저장한다.
                keys = litertable.literalList.keys()

                for key in keys:
                    literal = litertable.getLiteral(key)
                    if key[1] == 'X':
                        self.codList.append(literal)
                    elif key[1] == 'C':
                        temp = ""
                        for k in range(len(literal)):
                            temp = temp + "%X"%int(literal[k])          # char를 아스키 코드로 변환하여 string으로 저장한다.
                        self.codList.append(temp)

            self.sec_codeList.append(self.codList)

            self.codList = []




    def outputSymbolTable(self,fileName):           #symbol 테이블을 파일에 저장한다.
        file = open(fileName,'w')

        for i in range(len(self.symtabList)):
            symtab = self.symtabList[i]

            for j in range(len(symtab.symbolList)):
                data = "%s\t%4X\n" % (symtab.symbolList[j],symtab.locationList[j])
                file.write(data)

            file.write("\n")

        file.close()

    def printSymbolTable(self,fileName):
        file = open(fileName, 'r')
        while True:
            line = file.readline()
            if not line: break
            word = line.split('\t\n')
            if len(word) == 2:
                string = word[0] + "\t" + word[1]
                print(string)
            else:
                line = line[:-1]
                print(line)
        file.close()

    def printObjectCode(self,fileName):
        count = 0
        tokenTable = []
        toklist = []
        token = ""
        file = open(fileName, 'w')

        for i in range(len(self.TokenList)):        #tokentable 을 sect 별로 접근한다.
            codelist = self.sec_codeList[i]         #현재 sect의 codeList를 저장한다.
            tokenTable = self.TokenList[i]          #현재 sect의 tokenTable을 저장한다.
            toklist = tokenTable.tokenList
            l = m = 0

            for j in range(len(toklist)):       # 현재 sect의 tokenlist에 접근한다.
                token = toklist[j]

                if j==0:                        #H recode를 저장하기 위해
                    length = 0
                    if toklist[len(toklist)-1].operator == "EQU" :
                        length = toklist[len(toklist)-2].location
                    elif toklist[len(toklist)-1].operator == "END":
                        length = toklist[len(toklist)-2].location + 4
                    else:
                        length = toklist[len(toklist)-1].location + 3

                    hline = "H%-6s%06X%06X"% (token.label,token.location,length)
                    file.write(hline)
                    file.write("\n")

                elif token.operator !="" and token.operator == "EXTDEF":        #D recode를 출력한다.
                    address = 0
                    define = ""
                    temp = "D"

                    for k in range(len(tokenTable.extdef)):
                        address = tokenTable.symTab.search(tokenTable.extdef[k])
                        define = "%-6s%06X"%(tokenTable.extdef[k],address)
                        temp = temp + define

                    file.write(temp)
                    file.write("\n")

                elif token.operator != "" and token.operator == "EXTREF":       #R recode를 출력한다.
                    refine = ""
                    temp = "R"

                    for k in range(len(tokenTable.extref)):
                        refine = "%-6s"%tokenTable.extref[k]
                        temp = temp + refine

                    file.write(temp)
                    file.write("\n")

                elif token.label != "." and count != len(codelist)-1:       #codeList에 있는 objectcode를 T recode에 출력한다.
                    temp = "T"
                    start_loc = "%06X"%token.location       #시작 주소를 저장한다.
                    temp = temp + start_loc
                    cnt = 0

                    while l<len(codelist):          #T recode 한 줄의 objectcode 길이를 구한다.
                        cnt += len(codelist[l])
                        if cnt>=60:                 #길이가 60을 넘으면 루프를 빠져 나온다.
                            break
                        l += 1

                    if l == len(codelist):          #길이가 60보다 작으면 바로 2로 나눈다.
                        cnt/=2
                    else:
                        cnt = (cnt - len(codelist[l]))/2    #길이가 60보다 크면 바로 전 objectcode의 길이를 빼준 뒤 2로 나눈다.

                    len_ = "%02X"%int(cnt)      #한 line의 objectcode 길이를 len에 저장한 뒤 temp 변수에 붙여준다.
                    temp = temp + len_

                    while m<l:                      # temp 변수에 objectcode들을 이어붙인다.
                        temp = temp + codelist[m]
                        m+=1

                    j+=l-1                  #j 변수에 읽은 objectcode 라인 수를 저장함으로써 중복되지 않도록 한다.
                    count = l-1             #count 변수로  codeList에 있는 objectcode가 중복되어 출력되지 않도록 한다.

                    file.write(temp)
                    file.write("\n")

            if tokenTable.literTab.ltorg_flag == True:          #RESW 다음에 있는 리터럴을 T recode에 출력한다.
                string = "T"
                litertable = self.TokenList[i].literTab
                temp = ""

                for key in litertable.literalList.keys():
                    literal = litertable.getLiteral(key)
                    tp = "%06X%02X"%(litertable.literalList[key],len(literal))
                    string = string + tp

                    if key[1] == 'X':
                        codelist.append(literal)
                    elif key[1] == 'C':             #ex) =C'EOF' 의 경우 objectcode는 아스키코드로 저장되므로 문자를 아스키코드로 바꿔준다.
                        for k in range(len(literal)):
                            tp2 = "%X"%ord(literal[k])
                            temp = temp + tp2

                        string = string + temp

                file.write(string)
                file.write("\n")

            for j in range(len(toklist)):           # M recode를 출력한다.
                token = toklist[j]

                if len(token.objectCode) == 8 :     #외부의 변수를 참조하는 경우
                    modi = "M"
                    line = "%06X05+%-6s"%(token.location+1,token.operand[0])

                    modi = modi + line

                    file.write(modi)
                    file.write("\n")

                elif token.label == "MAXLEN" and token.operator != "EQU":   #MAXLEN 같이 외부 변수의 연산이 필요한 경우
                    word = token.operand[0].split('-')

                    modi = "M"
                    line = "%06X%02X+%-6s"%(token.location, len(word[0]), word[0])
                    modi = modi + line

                    file.write(modi)
                    file.write("\n")

                    modi = "M"
                    line = "%06X%02X-%-6s"%(token.location, len(word[1]), word[1])
                    modi = modi + line

                    file.write(modi)
                    file.write("\n")

            start_address = "%06X"%toklist[0].location
            file.write("E"+start_address)
            file.write("\n\n")

        file.close()



assembler = Assembler("inst.data")
assembler.loadInputFile("input.txt")


assembler.pass1()

assembler.printSymbolTable("symtab_20160333")

assembler.pass2()

assembler.printObjectCode("output_20160333")




