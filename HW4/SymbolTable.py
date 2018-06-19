#symbo과 관련된 데이터와 연산을 소유한다. section 별로 하나씩 인스턴스를 할당한다.
class SymbolTable:
    symbolList = []
    locationList = []

    def __init__(self):
        self.symbolList = []
        self.locationList = []

#새로운 symbol을 table에 추가한다.
    def putSymbol(self, symbol, location):
        if self.search(symbol) == -1:
            self.symbolList.append(symbol)
            self.locationList.append(location)
        else:
            print("The current index exists in the symbolTable")

#인자로 전달된 symbol이 어떤 주소를 지정하는지 알려준다.
    def search(self,symbol):
        index = 0
        if symbol in self.symbolList:
            index = self.symbolList.index(symbol)
            return self.locationList[index]
        else:
            return -1

#기존에 존재하는 symbol 값에 대해서 가리키는 주소값을 변경한다.
    def modifySymbol(self,symbol,newLocation):
        index = self.search(symbol)

        if index != -1:
            self.locationList[index] = newLocation
