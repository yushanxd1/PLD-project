class User:
    def __init__(self, name):
        self.name = name
        self.sheets = {}

    def create_sheet(self, name):
        self.sheets[name] = Sheet(name)

    def share_sheet(self, sheet, user, access):
        sheet.share(user, access)


class Sheet:
    def __init__(self, name):
        self.name = name
        self.grid = [[0 for _ in range(3)] for _ in range(3)]
        self.access_rights = {}

    def change_value(self, user, row, col, value):
        if user in self.access_rights and self.access_rights[user] == 'ReadOnly':
            print("This sheet is not accessible.")
        else:
            self.grid[row][col] = value

    def share(self, user, access):
        self.access_rights[user] = access

    def print_sheet(self, user):
        print()
        for row in self.grid:
            print(", ".join(map(str, row)))
        print()


def main():
    users = {}

    while True:
        print("---------------Menu---------------")
        print("1. Create a user")
        print("2. Create a sheet")
        print("3. Check a sheet")
        print("4. Change a value in a sheet")
        print("5. Change a sheet's access right")
        print("6. Collaborate with another user")
        print("----------------------------------")

        choice = input("> ")

        if choice == "1":
            name = input("> ")
            users[name] = User(name)
            print(f"Create a user named \"{name}\".\n")

        elif choice == "2":
            user_sheet = input("> ").split()
            user_name = user_sheet[0]
            sheet_name = user_sheet[1]
            users[user_name].create_sheet(sheet_name)
            print(f"Create a sheet named \"{sheet_name}\" for \"{user_name}\".\n")

        elif choice == "3":
            user_sheet = input("> ").split()
            user_name = user_sheet[0]
            sheet_name = user_sheet[1]
            users[user_name].sheets[sheet_name].print_sheet(user_name)

        elif choice == "4":
            user_sheet = input("> ").split()
            user_name = user_sheet[0]
            sheet_name = user_sheet[1]
            users[user_name].sheets[sheet_name].print_sheet(user_name)

            row, col, value = input("> ").split()
            try:
                value = eval(value)
            except Exception as e:
                print("Invalid arithmetic expression:", e)
                continue
            users[user_name].sheets[sheet_name].change_value(user_name, int(row), int(col), value)
            users[user_name].sheets[sheet_name].print_sheet(user_name)


        elif choice == "5":
            user_sheet_acc = input("> ").split()
            user_name = user_sheet_acc[0]
            sheet_name = user_sheet_acc[1]
            access = user_sheet_acc[2]
            users[user_name].sheets[sheet_name].access_rights[user_name] = access
            print()

        elif choice == "6":
            user_sheet_col = input("> ").split()
            user_name = user_sheet_col[0]
            sheet_name = user_sheet_col[1]
            col_name = user_sheet_col[2]
            users[user_name].sheets[sheet_name].share(col_name, access)
            print(f"Share \"{user_name}\"'s \"{sheet_name}\" with \"{col_name}\".\n")

        else:
            print("Invalid choice. Please try again.\n")


if __name__ == "__main__":
    main()
