class LiteralTable:
    literalList = dict()

    ltorg_flag = False

    def __init__(self):
        self.literalList = dict()
        self.ltorg_flag = False

#literalList에 리터럴이 없는 경우 리터럴을 넣어준다.
    def putLiteral(self,symbol,location):
        if self.searchLiteral(symbol) == -1:
            self.literalList[symbol] = location

#해당 literalList에 리터럴이 있는지 검색한다.
    def searchLiteral(self,symbol):
        address = self.literalList.get(symbol, -1)

        return address

#리터럴에서 실질적인 string을 리턴하는 메소드이다.
#ex) =C'EOF' 의 경우 EOF를 리턴한다.
    def getLiteral(self,literal):
        temp = literal

        if literal[0] == '=':
            temp = literal[3:-1]
        elif literal[0] == 'X':
            temp = literal[2:-1]

        return temp


