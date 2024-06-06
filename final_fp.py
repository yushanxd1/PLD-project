import operator
from collections import defaultdict

# Basic Data Structures
users = {}
sheets = {}

# Helper Functions
def create_user(name):
    if name not in users:
        users[name] = {"sheets": []}
        print(f'Create a user named "{name}".')
    else:
        print(f'User "{name}" already exists.')

def create_sheet(user, sheet_name):
    if user in users:
        if sheet_name not in sheets:
            sheet = {
                "owner": user,
                "data": [[0]*3 for _ in range(3)], # 3x3 sheet initialized with zeros
                "access": {user: "editable"},
            }
            sheets[sheet_name] = sheet
            users[user]["sheets"].append(sheet_name)
            print(f'Create a sheet named "{sheet_name}" for "{user}".')
        else:
            print(f'Sheet "{sheet_name}" already exists.')
    else:
        print(f'User "{user}" does not exist.')

def print_sheet(user, sheet_name):
    if sheet_name in sheets:
        sheet = sheets[sheet_name]
        if user in sheet["access"] and sheet["access"][user] in {"editable", "readonly"}:
            for row in sheet["data"]:
                print(", ".join(map(str, row)))
        else:
            print("This sheet is not accessible.")
    else:
        print(f'Sheet "{sheet_name}" does not exist.')

def change_sheet_value(user, sheet_name, row, col, value):
    if sheet_name in sheets:
        sheet = sheets[sheet_name]
        if user in sheet["access"] and sheet["access"][user] == "editable":
            try:
                parsed_value = eval(value, {"__builtins__": None}, {"__name__": None})
                sheet["data"][row][col] = parsed_value
            except Exception as e:
                print(f"Error evaluating value: {e}")
        else:
            print("This sheet is not editable.")
    else:
        print(f'Sheet "{sheet_name}" does not exist.')

def change_access_rights(user, sheet_name, access_type):
    if sheet_name in sheets:
        sheet = sheets[sheet_name]
        if user == sheet["owner"]:
            sheet["access"] = {user: access_type}
        else:
            print("Only the owner can change access rights.")
    else:
        print(f'Sheet "{sheet_name}" does not exist.')

def share_sheet(owner, sheet_name, other_user):
    if sheet_name in sheets:
        sheet = sheets[sheet_name]
        if owner == sheet["owner"]:
            sheet["access"][other_user] = "readonly"
            users[other_user]["sheets"].append(sheet_name)
            print(f'Share "{owner}"\'s "{sheet_name}" with "{other_user}".')
        else:
            print("Only the owner can share the sheet.")
    else:
        print(f'Sheet "{sheet_name}" does not exist.')

# Main Menu Logic
def main_menu():
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
            create_user(name)
        
        elif choice == "2":
            user = input("> ")
            sheet_name = input("> ")
            create_sheet(user, sheet_name)
        
        elif choice == "3":
            user = input("> ")
            sheet_name = input("> ")
            print_sheet(user, sheet_name)
        
        elif choice == "4":
            user = input("> ")
            sheet_name = input("> ")
            print_sheet(user, sheet_name)
            coordinates = input("> ").split()
            row, col = int(coordinates[0]), int(coordinates[1])
            value = coordinates[2]
            change_sheet_value(user, sheet_name, row, col, value)
            print_sheet(user, sheet_name)
        
        elif choice == "5":
            user = input("> ")
            sheet_name = input("> ")
            access_type = input("> ")
            change_access_rights(user, sheet_name, access_type)
        
        elif choice == "6":
            owner = input("> ")
            sheet_name = input("> ")
            other_user = input("> ")
            share_sheet(owner, sheet_name, other_user)
        
        else:
            print("Invalid choice. Please try again.")

if __name__ == "__main__":
    main_menu()
