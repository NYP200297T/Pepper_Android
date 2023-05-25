import time
import customtkinter
from tkinter import filedialog

class FirstMenu(customtkinter.CTk):
    def __init__(self):
        super().__init__()

        self.fp = None

        self.title("DynaFlow Builder")
        self.geometry("400x260")
        self.resizable(width=False, height=False)

        self.grid_rowconfigure(0, weight=1)
        self.grid_columnconfigure(0, weight=1)

        self.first_menu = customtkinter.CTkFrame(self, corner_radius=0)
        self.first_menu.pack(expand=True)

        self.choose_button = customtkinter.CTkButton(master=self.first_menu, height=40, anchor="center", border_spacing=10, text="Choose JSON File", command=self.choose_button_event)
        self.choose_button.grid(row=0, column=0, pady=10)

        self.create_button = customtkinter.CTkButton(master=self.first_menu, height=40, anchor="center", border_spacing=10, text="Create new flow", command=self.create_button_event)
        self.create_button.grid(row=1, column=0, pady=10)

    def choose_button_event(self):
        self.fp = filedialog.askopenfile(mode='r', filetypes=[("Json", '*.json')])
        self.destroy()

    def create_button_event(self):
        self.destroy()