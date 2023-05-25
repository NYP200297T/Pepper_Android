import json
import customtkinter
import tkinter
from PIL import Image
from fragment_frames import PicTextViewFrame

# Pepper tablet size - 1280*800
W, H = (1280,800)

class App(customtkinter.CTk):
    def __init__(self):
        super().__init__()
        self.jsondict = {"fragments": {}}
        self.fragments = {}

        # variables
        self.newview = "PicTextView"
        self.menumodes = ["PicTextView", "MenuView", "NavigateView"]
        self.null_image = customtkinter.CTkImage(Image.new("RGB", (W, H), (255, 255, 255)), size=(W,H))

        # set window attributes
        self.title("DynaFlow Builder")
        self.geometry("1600x900")

        self.grid_rowconfigure(0, weight=1)
        self.grid_columnconfigure(1, weight=1)

        self.navigation_frame = customtkinter.CTkFrame(self, corner_radius=0)
        self.navigation_frame.grid(row=0, column=0, sticky="nsew")

        self.controlpanel_frame = customtkinter.CTkFrame(self, corner_radius=0)
        self.controlpanel_frame.grid(row=0, column=1, sticky="nsew", padx=10, pady=10)

        self.navigation_frame_label = customtkinter.CTkLabel(self.navigation_frame, text="DynaFlow",
                                                             compound="center", font=customtkinter.CTkFont(size=15, weight="bold"))
        self.navigation_frame_label.grid(row=0, column=0, padx=20, pady=20, sticky="n")

        # scrollview for views
        self.views_frame = customtkinter.CTkFrame(self.navigation_frame, corner_radius=0)
        self.views_frame.grid_rowconfigure(0, weight=1)
        self.views_frame.grid_columnconfigure(0, weight=1)
        self.views_frame.grid(row=1, column=0, sticky='nsew')
        self.views_frame.propagate(False)

        # Add a canvas in that frame
        self.canvas = customtkinter.CTkCanvas(self.views_frame)
        self.canvas.grid(row=0, column=0, sticky="nsew")

        self.vsb = customtkinter.CTkScrollbar(self.views_frame, orientation="vertical", command=self.canvas.yview)
        self.vsb.grid(row=0, column=1, sticky='ns')
        self.canvas.configure(yscrollcommand=self.vsb.set)
        self.canvas.configure(scrollregion=self.canvas.bbox("all"))

        self.view_type_menu = customtkinter.CTkOptionMenu(self.navigation_frame, values=self.menumodes, command=self.set_newview)
        self.view_type_menu.set("PicTextView")
        self.view_type_menu.grid(row=2, column=0, padx=20, pady=10, sticky="ns")

        self.create_view_button = customtkinter.CTkButton(self.navigation_frame, text="New View", command=self.create_new_view)
        self.create_view_button.grid(row=3, column=0, padx=20, pady=10, sticky="ns")

        self.navigation_frame.grid_rowconfigure(1,weight=1)
        self.frame_viewbuttons = customtkinter.CTkFrame(self.canvas)
        self.canvas.create_window((0, 0), window=self.frame_viewbuttons, anchor='nw')

        # pictextview frame
        self.pictextview_frame = PicTextViewFrame(self.controlpanel_frame)
        self.pictextview_frame.grid_forget()

        # menuview frame
        self.menuview_frame = customtkinter.CTkFrame(self.controlpanel_frame)
        self.pictextview_frame.grid_forget()

        # navigateview frame
        self.navigateview_frame = customtkinter.CTkFrame(self.controlpanel_frame)
        self.pictextview_frame.grid_forget()

        # flow settings frame
        self.settings_frame = customtkinter.CTkFrame(self.controlpanel_frame)
        self.pictextview_frame.grid_forget()

        settings_button = customtkinter.CTkButton(self.frame_viewbuttons, corner_radius=0, height=40, border_spacing=10, text="Flow Settings",
                                                   fg_color="transparent", text_color=("gray10", "gray90"), hover_color=("gray70", "gray30"),
                                                   anchor="center", command=lambda m="flow_settings": self.update_view_selection(m))
        settings_button.grid(row=0, column=0, sticky="ew")

    def set_newview(self, newview):
        self.newview = newview

    def create_new_view(self):
        self.view_type_menu.configure(state="disabled")
        frag_json = self.jsondict.get("fragments")
        #naming convention
        if frag_json != {} and frag_json != None:
            similar_frags = [i for i in sorted(list(frag_json.keys())) if frag_json[i].get("kind") == self.newview.lower()]
            new_name = self.newview.lower() + str(len(similar_frags)+1)
        else:
            new_name = self.newview.lower() + "1"
        self.jsondict["fragments"].update({new_name: {"kind": self.newview.lower()}})
        self.add_view(new_name)
        self.view_type_menu.configure(state="normal")

    def add_view(self,name):
        custom_null_img = self.null_image
        custom_null_img.configure(size=(320,200))
        new_view = customtkinter.CTkButton(self.frame_viewbuttons, corner_radius=0, height=40, border_spacing=10, text=name,
                                                   fg_color="transparent", text_color=("gray10", "gray90"), hover_color=("gray70", "gray30"),
                                                   image=custom_null_img, anchor="w", command=lambda m=name: self.update_view_selection(m))
        new_view.grid(row=len(self.frame_viewbuttons.children.keys()), column=0, sticky="ew")
        self.fragments[name] = new_view
        self.update_canvas()

    def remove_view(self,name):
        self.fragments[name].grid_forget()
        self.update_canvas()

    def update_canvas(self):
        # Update buttons frames idle tasks to let tkinter calculate buttons sizes
        self.frame_viewbuttons.update_idletasks()

        # Resize the canvas frame to show exactly 5-by-5 buttons and the scrollbar
        total_width = sorted([self.frame_viewbuttons.children[i].winfo_width() for i in self.frame_viewbuttons.children if isinstance(self.frame_viewbuttons.children[i],customtkinter.CTkButton)])[-1]
        total_height = sum([self.frame_viewbuttons.children[i].winfo_height() for i in self.frame_viewbuttons.children if isinstance(self.frame_viewbuttons.children[i],customtkinter.CTkButton)])
        self.canvas.configure(width=total_width, height=total_height)
        self.canvas.configure(scrollregion=self.canvas.bbox("all"))

    def mainloop(self, fp=None, *args, **kwargs):
        self.fp = fp
        if fp != None:
            with open(fp,"r") as f:
                self.jsondict = json.load(f)
                self.fragments = self.jsondict.get("fragments")
                if self.jsondict.get("fragments") == None:
                    print("Invalid JSON")
                    exit()
                for frag in self.jsondict.get("fragments"):
                    self.add_view(frag)            
        return super().mainloop(*args, **kwargs)

    def update_view_selection(self,name):
        for n in self.frame_viewbuttons.children:
            widget = self.frame_viewbuttons.children[n]
            if isinstance(widget, customtkinter.CTkButton):
                widget.configure(fg_color=("gray75", "gray25") if widget.cget("text") == name else "transparent")

        if name == "flow_settings":
            self.settings_frame.grid(row=0, column=0, sticky="nsew")
        else:
            view_type = self.jsondict["fragments"][name]["kind"]
            self.settings_frame.grid_forget()

            # show selected frame
            if view_type == "pictextview":
                self.pictextview_frame.read_json(self.jsondict, name)
                self.pictextview_frame.grid(row=0, column=0, sticky="nsew")
            else:
                self.pictextview_frame.grid_forget()
            if view_type == "menuview":
                self.menuview_frame.grid(row=0, column=0, sticky="nsew")
                # self.menuview_frame.read_json(self.jsondict, name)
            else:
                self.menuview_frame.grid_forget()
            if view_type == "navigateview":
                self.navigateview_frame.grid(row=0, column=0, sticky="nsew")
                # self.navigateview_frame.read_json(self.jsondict, name)
            else:
                self.navigateview_frame.grid_forget()