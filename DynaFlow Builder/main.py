import customtkinter
from app import App

# set customtkinter colour based on theme
customtkinter.set_appearance_mode("System")
customtkinter.set_default_color_theme("blue")

# runs main app
if __name__ == "__main__":
    app = App()
    app.mainloop()