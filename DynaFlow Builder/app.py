# UI imports
import customtkinter
from tkinter import filedialog, PhotoImage
from tkinter.messagebox import askyesno
# file imports
import os
import shutil
import json
# image import
from PIL import Image
# other frames
from fragment_frames import PicTextViewFrame, NavigationViewFrame, MenuViewFrame
from adb_frame import AdbFrame

# Pepper tablet size - 1280*800
W, H = (1280,800)
iconsize = (240,150) # size of image icon in sidebar

class App(customtkinter.CTk):

    def __init__(self):
        # first menu startup
        super().__init__()

        # set variables
        self.fp = None
        self.adb_framer = None

        photo = PhotoImage(file=os.path.join(os.getcwd(), "icon.png"))
        self.wm_iconphoto(False, photo)

        # window settings
        self.title("DynaFlow Builder")
        self.geometry("400x320")

        # set UI elements
        self.first_menu = customtkinter.CTkFrame(self, corner_radius=0)
        self.first_menu.pack(expand=True)

        self.choose_button = customtkinter.CTkButton(master=self.first_menu, height=40, anchor="center", border_spacing=10, text="Choose JSON File", command=self.choose_button_event)
        self.choose_button.pack(pady=10)

        self.create_button = customtkinter.CTkButton(master=self.first_menu, height=40, anchor="center", border_spacing=10, text="Create new flow", command=self.init)
        self.create_button.pack(pady=10)

        self.internal_button = customtkinter.CTkButton(master=self.first_menu, height=40, anchor="center", border_spacing=10, text="Read internal files", command=self.internal_button_event)
        self.internal_button.pack(pady=10)

    # choose JSON event
    def choose_button_event(self):
        self.fp = filedialog.askopenfilename(filetypes=[("Json", '*.json')])
        if self.fp != "":
            self.init()
        else:
            self.fp = None

    # read internal files (/DynaFlow/ and /Maps/)
    def internal_button_event(self):
        internal_json_path = os.path.join(os.getcwd(), "DynaFlow", "flow.json")
        if os.path.exists(internal_json_path):
            self.fp = internal_json_path
            self.restart_app()
            internal_points_path = os.path.join(os.getcwd(), "Maps", "points.json")
            if os.path.exists(internal_points_path):
                self.load_points(internal_points_path)
        else:
            self.internal_button.configure(state='disabled')

    # start main application via init()
    def restart_app(self):
        if self.adb_framer != None:
            self.adb_framer.destroy()
        for view in list(self.children.values()):
            view.destroy()
        self.init()

    # main app method
    def init(self):

        # reset window to main app
        self.first_menu.pack_forget()
        self.geometry("1280x800")

        # initalise variables
        self.jsondict = {"fragments": {}, "imagepath": "/DynaFlow/images/"}
        self.fragments = {}
        self.current_viewer = None
        self.current_name = None
        self.pointsjson = None
        self.null_image = customtkinter.CTkImage(Image.new("RGB", (W, H), (255, 255, 255)), size=(W,H))

        # create folder first
        local_dyna = os.path.join(os.getcwd(), "DynaFlow")
        if not os.path.exists(local_dyna):
            os.mkdir(local_dyna)

        # variables for views
        self.newview = "PicTextView"
        self.menumodes = ["PicTextView", "MenuView"]
        # self.menumodes = ["PicTextView", "MenuView", "NavigationView"]

        # configure window grid
        self.grid_rowconfigure(0, weight=1)
        self.grid_columnconfigure(1, weight=1)

        # navigation sidebar frame
        self.navigation_frame = customtkinter.CTkFrame(self, corner_radius=0)
        self.navigation_frame.grid(row=0, column=0, sticky="nsew")

        # working window frame
        self.controlpanel_frame = customtkinter.CTkFrame(self, corner_radius=0)
        self.controlpanel_frame.grid(row=0, column=1, sticky="nsew", padx=10, pady=10)

        # DynaFlow label on top of side bar
        self.navigation_frame_label = customtkinter.CTkLabel(self.navigation_frame, text="DynaFlow",
                                                             compound="center", font=customtkinter.CTkFont(size=15, weight="bold"))
        self.navigation_frame_label.grid(row=0, column=0, padx=20, pady=20, sticky="n")

        # scrollview for views
        self.views_frame = customtkinter.CTkFrame(self.navigation_frame, corner_radius=0)
        self.views_frame.grid_rowconfigure(0, weight=1)
        self.views_frame.grid_columnconfigure(0, weight=1)
        self.views_frame.grid(row=1, column=0, sticky='nsew')

        # Add a canvas in scollview frame
        self.canvas = customtkinter.CTkCanvas(self.views_frame)
        self.canvas.configure(bg=self.cget("bg"))
        self.canvas.grid(row=0, column=0, sticky="nsew")

        # add scrollbar
        self.vsb = customtkinter.CTkScrollbar(self.views_frame, orientation="vertical", command=self.canvas.yview)
        self.vsb.grid(row=0, column=1, sticky='ns')
        self.canvas.configure(yscrollcommand=self.vsb.set)
        self.canvas.configure(scrollregion=self.canvas.bbox("all"))

        # dropdown menu for choosing Fragment to add
        self.view_type_menu = customtkinter.CTkOptionMenu(self.navigation_frame, values=self.menumodes, command=self.set_newview)
        self.view_type_menu.set("PicTextView")
        self.view_type_menu.grid(row=2, column=0, pady=10, sticky="ns")

        # add New View button to sidebar
        self.create_view_button = customtkinter.CTkButton(self.navigation_frame, text="New View", command=self.create_new_view)
        self.create_view_button.grid(row=3, column=0, pady=10, sticky="ns")

        # add Delete View button to sidebar
        self.delete_view_button = customtkinter.CTkButton(self.navigation_frame, text="Delete View", command=self.remove_view)
        self.delete_view_button.grid(row=4, column=0, pady=10, sticky="ns")
        self.delete_view_button.configure(state="disabled")

        # set sidebar attributes
        self.navigation_frame.grid_rowconfigure(1,weight=1)
        self.frame_viewbuttons = customtkinter.CTkFrame(self.canvas)
        self.canvas.create_window((0, 0), window=self.frame_viewbuttons, anchor='nw')

        # add Flow Settings to sidebar
        settings_button = customtkinter.CTkButton(self.frame_viewbuttons, corner_radius=0, height=40, border_spacing=10, text="Flow Settings",
                                                   fg_color="transparent", text_color=("gray10", "gray90"), hover_color=("gray70", "gray30"),
                                                   anchor="center", command=lambda m="Flow Settings": self.update_view_selection(m))
        settings_button.grid(row=0, column=0, sticky="ew")

        # pictextview frame
        self.pictextview_frame = PicTextViewFrame(self.controlpanel_frame)

        # menuview frame
        self.menuview_frame = MenuViewFrame(self.controlpanel_frame)

        # navigateview frame
        self.navigateview_frame = NavigationViewFrame(self.controlpanel_frame)

        # flow settings frame
        self.settings_frame = customtkinter.CTkFrame(self.controlpanel_frame)
        self.settings_frame.grid_rowconfigure(0, weight=1)
        self.settings_frame.grid_columnconfigure(1, weight=1)

        self.lsettings_frame = customtkinter.CTkFrame(self.settings_frame)
        self.lsettings_frame.grid(row=0, column=0, sticky="nsew")

        # points frame
        self.map_frame = customtkinter.CTkFrame(self.lsettings_frame)
        self.map_frame.grid_rowconfigure(0, weight=1)
        self.map_frame.grid_columnconfigure((0,1), weight=1)
        
        self.points_label = customtkinter.CTkLabel(self.map_frame, text="Not loaded")
        self.points_label.grid(row=0, column=0, padx=10, pady=10)
        self.points_button = customtkinter.CTkButton(self.map_frame, text="Choose points.json", command=self.open_points)
        self.points_button.grid(row=0, column=1, padx=10, pady=10)
        
        self.map_frame.pack(padx=10, pady=10)

        # home fragment frame
        self.homefrag_frame = customtkinter.CTkFrame(self.lsettings_frame)

        self.homefrag_label = customtkinter.CTkLabel(self.homefrag_frame, text="Home fragment", anchor="center")
        self.homefrag_label.grid(row=0, column=0, stick= "nsew", padx=10, pady=10)
        self.home_chooser = customtkinter.CTkOptionMenu(self.homefrag_frame, values=[], command=self.set_homefrag)
        self.home_chooser.grid(row=0, column=1, stick= "nsew", padx=5, pady=10)
        
        self.homefrag_frame.pack(padx=10, pady=10)

        # adb frame
        self.adb_framer = AdbFrame(self.lsettings_frame)
        self.adb_framer.pack(padx=10, pady=10)

        # extra button
        self.refresh_files_button = customtkinter.CTkButton(self.lsettings_frame, text="Load internal files", command=self.internal_button_event)
        self.refresh_files_button.pack(padx=10, pady=10)

        self.delete_files_button = customtkinter.CTkButton(self.lsettings_frame, text="Delete internal files", command=self.delete_internal)
        self.delete_files_button.pack(padx=10, pady=10)

        # preview frame
        self.preview_frame = customtkinter.CTkFrame(self.settings_frame)

        self.preview_json = customtkinter.CTkTextbox(self.preview_frame, state="disabled", activate_scrollbars=True, wrap="none")
        self.preview_json.pack(expand=True, anchor="center", fill="both", padx=10, pady=10)
        self.save_json = customtkinter.CTkButton(self.preview_frame, text="Save JSON", command=self.set_save)
        self.save_json.pack(padx=10, pady=10, anchor="center", fill='x')

        self.preview_frame.grid(row=0, column=1, sticky="nsew", padx=10, pady=10)

        # load JSON file data if set
        if self.fp != None:
            with open(self.fp,"r") as f:
                # copy file into DynaFlow
                try:
                    shutil.copy(self.fp, os.path.join(os.getcwd(), "DynaFlow", "flow.json"))
                except shutil.SameFileError:
                    pass
                except Exception as e:
                    print(e)
                    pass
                self.jsondict = json.load(f)
                self.fragments = self.jsondict.get("fragments").copy()
                if self.jsondict.get("fragments") == None:
                    print("Invalid JSON")
                    exit()

                # load data from JSON
                for frag in self.jsondict.get("fragments"):
                    self.add_view(frag)

    # set new view type
    def set_newview(self, newview):
        self.newview = newview

    def set_homefrag(self, frag):
        self.jsondict.update({"start": frag})

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
        self.home_chooser_e()    

    def add_view(self, name):
        custom_null_img = self.null_image
        custom_null_img.configure(size=iconsize)
        new_view = customtkinter.CTkButton(self.frame_viewbuttons, corner_radius=0, height=40, border_spacing=10, text=name,
                                                   fg_color="transparent", text_color=("gray10", "gray90"), hover_color=("gray70", "gray30"),
                                                   image=custom_null_img, anchor="w", command=lambda m=name: self.update_view_selection(m))
        new_view.grid(row=len(self.frame_viewbuttons.children.keys()), column=0, sticky="ew")
        self.fragments[name] = new_view

        if self.jsondict["fragments"][name]["kind"] == "pictextview":
            tt = self.pictextview_frame
        elif self.jsondict["fragments"][name]["kind"] == "menuview":
            tt = self.menuview_frame
        elif self.jsondict["fragments"][name]["kind"] == "navigateview":
            tt = self.navigateview_frame
        else:
            tt = None

        if tt != None:
            tt.read_json(self.jsondict, name)
            self.fragments[name].configure(image=customtkinter.CTkImage(tt.imager, size=iconsize), require_redraw=True)

        self.update_canvas()
        if self.current_viewer != None and self.current_name != "Flow Settings":
            if self.current_viewer.push_fragments != None:
                fragments = list(self.fragments.keys())
                if self.jsondict.get("start") != None:
                    fragments.remove(self.jsondict["start"])
                    if self.jsondict["start"] != self.current_name:
                        fragments = ["home"] + fragments
                self.current_viewer.push_fragments(fragments)

    def remove_view(self):
        self.fragments[self.current_name].destroy()
        del self.fragments[self.current_name]
        del self.jsondict["fragments"][self.current_name]
        self.current_name = None
        self.current_viewer.pack_forget()
        self.current_viewer = None
        self.update_canvas()

    def update_canvas(self):
        # Update buttons frames idle tasks to let tkinter calculate buttons sizes
        self.frame_viewbuttons.update_idletasks()

        total_width = sorted([self.frame_viewbuttons.children[i].winfo_width() for i in self.frame_viewbuttons.children if isinstance(self.frame_viewbuttons.children[i],customtkinter.CTkButton)])[-1]
        total_height = sum([self.frame_viewbuttons.children[i].winfo_height() for i in self.frame_viewbuttons.children if isinstance(self.frame_viewbuttons.children[i],customtkinter.CTkButton)])
        self.canvas.configure(width=total_width, height=total_height)
        self.canvas.configure(scrollregion=self.canvas.bbox("all"))

    def refresh_preview(self):
        self.preview_json.configure(state="normal")
        self.preview_json.delete("0.0", "end")
        json_str = json.dumps(self.jsondict, ensure_ascii=True, indent=4)
        self.preview_json.insert("0.0", text=json_str)
        self.preview_json.configure(state="disabled")

    def home_chooser_e(self):
        vals = list(self.jsondict.get("fragments").keys())
        if self.jsondict.get("start") != None:
            vals.remove(self.jsondict["start"])    
            self.home_chooser.set(self.jsondict["start"])
        else:
            self.home_chooser.set("")
        self.home_chooser.configure(values=vals)

    def read_settings(self):
        self.refresh_preview()
        self.home_chooser_e()
        
    def set_save(self):
        jsonfilepath = os.path.join(os.getcwd(), "DynaFlow", "flow.json")
        if os.path.exists(jsonfilepath):
            tt = askyesno("Confirmation", "Save will replace old file. Proceed?")
            if not tt:
                return
            
        with open(jsonfilepath, mode="w+") as writejson:
            json.dump(self.jsondict, writejson, indent=4)

    def open_points(self):
        points_fp = filedialog.askopenfilename(filetypes=[("Json", '*.json')])
        if points_fp != "" and points_fp != None:
            self.load_points(points_fp)
        self.refresh_preview()

    def load_points(self, points_fp):
        with open(points_fp, "r", errors='ignore') as points_json:
            map_pather = os.path.join(os.getcwd(), "Maps")
            point_in_fp = os.path.join(map_pather, "points.json")
            if not os.path.exists(map_pather):
                os.mkdir(map_pather)
            try:
                shutil.copy(points_fp, point_in_fp)
            except shutil.SameFileError:
                pass
            except Exception as e:
                print(e)
                pass
            self.pointsjson = self.read_points_json(points_json)
            print(self.pointsjson)
            self.view_type_menu.configure(values=self.menumodes+["NavigateView"])
            self.points_label.configure(text="Loaded")

    # points.json file contains some unwanted bytes at start of file
    def read_points_json(self,json_r):
        json_str = json_r.read()
        if not json_str.startswith("{"):
            partitioner = json_str.partition('"')
            rejson_str = "".join(partitioner[1:])
            rejson_str = '{'+rejson_str
            return json.loads(rejson_str)
        else:
            return json.load(json_r)

    def update_view_selection(self,name):

        self.delete_view_button.configure(state="disabled")

        for n in self.frame_viewbuttons.children:
            widget = self.frame_viewbuttons.children[n]
            if isinstance(widget, customtkinter.CTkButton):
                widget.configure(fg_color=("gray75", "gray25") if widget.cget("text") == name else "transparent")                    

        if self.current_viewer != None:
            if self.current_name != "Flow Settings":
                self.fragments[self.current_name].configure(image=customtkinter.CTkImage(self.current_viewer.imager, size=iconsize), require_redraw=True)
                self.current_viewer.pack_forget()
            else:
                self.current_viewer.pack_forget()

        self.current_name = name

        if name != "Flow Settings":
            view_type = self.jsondict["fragments"][name]["kind"]
        else:
            view_type = name
        
        if view_type == "Flow Settings":
            self.read_settings()
            self.settings_frame.pack(anchor="center", expand=True, fill="both")
            self.current_viewer = self.settings_frame

        if view_type == "pictextview":
            self.pictextview_frame.read_json(self.jsondict, name)
            self.pictextview_frame.pack(anchor="center", expand=True, fill="both")
            self.current_viewer = self.pictextview_frame
            self.delete_view_button.configure(state="normal")

        if view_type == "menuview":
            self.menuview_frame.read_json(self.jsondict, name)
            self.menuview_frame.pack(anchor="center", expand=True, fill="both")
            self.current_viewer = self.menuview_frame
            self.delete_view_button.configure(state="normal")

        if view_type == "navigateview":
            self.navigateview_frame.read_points(self.pointsjson)
            self.navigateview_frame.read_json(self.jsondict, name)
            self.navigateview_frame.pack(anchor="center", expand=True, fill="both")
            self.current_viewer = self.navigateview_frame
            self.delete_view_button.configure(state="normal")

    def delete_internal(self):
        tf = askyesno("Confirmation", "Are you sure you want to delete files?\nThis will restart the application without unsaved changes.")
        if tf:
            shutil.rmtree(os.path.join(os.getcwd(), "DynaFlow"))
            shutil.rmtree(os.path.join(os.getcwd(), "Maps"))
            self.restart_app()
