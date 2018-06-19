class Register:

    register = dict()

    def __init__(self):
        self.initRegister()

    def initRegister(self):
        self.register['A'] = 0
        self.register['X'] = 1
        self.register['L'] = 2
        self.register['B'] = 3
        self.register['S'] = 4
        self.register['T'] = 5
        self.register['F'] = 6
        self.register['PC'] = 8
        self.register['SW'] = 9

#reg가 레지스터 테이블에 있는지 검사하는 메소드이다.
#해당 테이블에 존재하면 주소값을 리턴하고, 없으면 -1을 리턴한다.
    def searchRegister(self,reg):
        if reg in self.register:
            return self.register[reg]
        else:
            return -1


