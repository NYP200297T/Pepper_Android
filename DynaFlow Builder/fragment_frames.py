import customtkinter
from tkinter import filedialog
from PIL import Image, ImageFont, ImageDraw
import shutil
import os

W, H = (1280,800) # pepper screen size
text_styler = {1: "Bold", 2: "Italic", 3: "BoldItalic"} # font file name, Java Font type uses int, easier this way
images_path = os.path.join(os.getcwd(), "DynaFlow", "images") # image path from folder
# (n)dp * tvdpi -> n*1.33 in pixels (px) idk why ask android
# converted using https://angrytools.com/android/pixelcalc/
bW, bH = (266, 266)
vspace = 80
hspace = 53
fontsize = 80
# coordinates of top right of button in menus 1-6
coord_dict = {
    1: {"button1": (W/2-bW/2, H/2-bH/2)},
    2: {"button1": (W/2-(bW*2+vspace)/2 , H/2-bH/2), "button2": (W/2+vspace/2 , H/2-bH/2)},
    3: {"button1": (W/2-(bW*3+vspace*2)/2 , H/2-bH/2), "button2": (W/2-bW/2, H/2-bH/2), "button3": (W/2+(vspace+bW/2) , H/2-bH/2)},
    4: {"button1": (W/2-(bW*2+vspace)/2 , H/2-bH-hspace/2), "button2": (W/2+vspace/2, H/2-bH-hspace/2), "button3": (W/2-(bW*2+vspace)/2 , H/2+hspace/2), "button4": (W/2+vspace/2, H/2+hspace/2)},
    5: {"button1": (W/2-(bW*3+vspace*2)/2 , H/2-bH-hspace/2), "button2": (W/2-bW/2, H/2-bH-hspace/2), "button3": (W/2+(vspace+bW/2) , H/2-bH-hspace/2), "button4": (W/2-(bW*2+vspace)/2, H/2+hspace/2), "button5": (W/2+vspace/2, H/2+hspace/2)},
    6: {"button1": (W/2-(bW*3+vspace*2)/2 , H/2-bH-hspace/2), "button2": (W/2-bW/2, H/2-bH-hspace/2), "button3": (W/2+(vspace+bW/2) , H/2-bH-hspace/2), "button4": (W/2-(bW*3+vspace*2)/2 , H/2+hspace/2), "button5": (W/2-bW/2, H/2+hspace/2), "button6": (W/2+(vspace+bW/2) , H/2+hspace/2)},
}

# Skeleton frame
# includes one Label for image view and one TabView for options
class SkeletonFrame(customtkinter.CTkFrame):

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)

        self.grid_columnconfigure(0, weight=1)
        self.grid_rowconfigure(0, weight=1)
        self.grid_rowconfigure(1, weight=2)
        
        # set frame for settings
        self.menu_viewer = customtkinter.CTkTabview(self)
        self.menu_viewer.grid_columnconfigure(0,weight=1)
        self.menu_viewer.grid_rowconfigure(0,weight=1)
        self.menu_viewer.grid(row=1, column=0, padx=5, pady=5, sticky="nsew")
        self.menu_viewer.grid_propagate(False)

    def add_menu_options(self, name:str):
        tab_frame = self.menu_viewer.add(name)
        return tab_frame
    
# BaseViewFrame includes General settings for all views
class BaseViewFrame(SkeletonFrame):

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)

        self.current_settings = "General"
        self.fragmentname = None
        self.chat_text = customtkinter.StringVar(self, value="") 

        self.null_image = Image.new("RGBA", (W, H), (255, 255, 255))
        # self.null_imgwidget = customtkinter.CTkImage(self.null_image, size=(W*0.6,H*0.6))

        self.grid_columnconfigure(0, weight=1)
        self.grid_rowconfigure(0, weight=1)
        self.grid_rowconfigure(1, weight=2)
        
        # set imageview original
        self.imageview = customtkinter.CTkLabel(self, text="", width=W*0.6, height=H*0.6,  anchor="center")
        self.imageview.grid(row=0, column=0, padx=5, pady=5)

        self.general_frame = self.add_menu_options("General")
        self.general_frame.grid_columnconfigure(0, weight=1)
        self.general_frame.grid_rowconfigure((0,1,2),weight=1)
        
        self.next_frame = customtkinter.CTkFrame(self.general_frame)
        self.next_text = customtkinter.CTkLabel(self.next_frame, text="Next fragment", anchor="center")
        self.next_text.grid(row=0, column=0, stick= "nsew", padx=10, pady=10)
        self.next_chooser = customtkinter.CTkOptionMenu(self.next_frame, values=[], command=self.set_next)
        self.next_chooser.grid(row=0, column=1, stick= "nsew", padx=5, pady=10)
        self.next_frame.grid(row=0, column=0, stick="ew", padx=10, pady=10)

        self.image_frame = customtkinter.CTkFrame(self.general_frame)
        self.new_image = customtkinter.CTkButton(self.image_frame, text="Choose Image", anchor="center", command=self.choose_image)
        self.new_image.grid(row=0, column=0, sticky="nsew", padx=10, pady=10)
        self.current_image = customtkinter.CTkLabel(self.image_frame, text="Chosen image: None", anchor="w")
        self.current_image.grid(row=0, column=1, sticky="nsew", padx=10, pady=10)
        self.image_frame.grid(row=1,column=0,sticky="ew", padx=10, pady=10)

        self.chat_frame = customtkinter.CTkFrame(self.general_frame)
        self.chatbox = customtkinter.CTkEntry(self.chat_frame, textvariable=self.chat_text, placeholder_text="Enter say dialog here", width=200, height=20)
        self.chatbox.grid(row=0, column=0, stick="nsew", padx=10, pady=10)
        self.set_chat = customtkinter.CTkButton(self.chat_frame, text="Set Say", command=self.set_say)
        self.set_chat.grid(row=0, column=1, stick="nsew", padx=10, pady=10)
        self.chat_frame.grid(row=2, column=0, sticky="ew", padx=10, pady=10)
        
        #center items and set frames
        self.menu_viewer.set("General")

    def read_json(self,json,fragment_name):
        # set self values
        self.json = json
        self.fragmentname = fragment_name
        self.fragmentjson = json["fragments"][fragment_name]

        # set existing data into widgets
        if self.fragmentjson.get("next") != None:
            self.next_chooser.set(self.fragmentjson["next"])
        else:
            self.next_chooser.set("")

        fragments = list(json["fragments"].keys())
        
        if self.json.get("start") != None:
            fragments.remove(self.json["start"])
            if self.json["start"] != self.fragmentname:
                fragments = ["home"] + fragments

        self.push_fragments(fragments)

        if self.fragmentjson.get("image") == None:
            self.current_image.configure(text=f"Current image: None")
            self.new_image.configure(text="Choose Image")
        else:
            self.current_image.configure(text=f"Current image: {self.fragmentjson['image']}")
            self.new_image.configure(text="Remove Image")

        if len(self.chat_text.get()) > 0:
            self.chatbox.delete(first_index=0, last_index=len(self.chat_text.get())+1)
        if self.fragmentjson.get("chat") != None:
            self.chatbox.insert(0, self.fragmentjson["chat"])

        self.update_image()
        self.set_image()

    def push_fragments(self,fragments):
        # updates list of fragments from sidebar into dropdown list
        if self.fragmentname != None:
            if self.fragmentname in fragments:
                fragments.remove(self.fragmentname)
            if len(fragments) != 0:
                self.next_chooser.configure(values=fragments)

    def write_data(self,data:dict):
        self.fragmentjson.update(data)

    def delete_data(self,key):
        if self.fragmentjson.get(key) != None:
            del self.fragmentjson[key]

    def choose_image(self):
        # toggle between Choose or Remove based on button text
        if self.new_image.cget("text") == "Choose Image":
            self.new_image.configure(text="Remove Image")

            fp = filedialog.askopenfilename(filetypes=[('Image', ('*.png', '*.jpg', '*.jpeg'))])
            if fp != "" and fp != None:
                f_name = str(fp).split("/")[-1]
                if not os.path.exists(images_path):
                    os.mkdir(images_path)
                try:
                    shutil.copy(fp, os.path.join(images_path, f_name))
                except shutil.SameFileError:
                    pass
                except Exception as e:
                    print(e)
                    pass
                self.write_data({"image": f_name})
            else:
                f_name = "None"

        else:
            f_name = "None"
            self.new_image.configure(text="Choose Image")
            self.delete_data("image")

        self.current_image.configure(text=f"Current image: {f_name}")

        self.update_image()
        self.set_image()

    def update_image(self):
        self.imager = self.null_image.copy()
        if self.fragmentjson.get("image") != None:
            if os.path.exists(os.path.join(images_path, self.fragmentjson['image'])):
                paster = Image.open(os.path.join(images_path, self.fragmentjson['image'])).convert("RGBA").resize((W,H))
                self.imager.paste(paster, (0,0), mask=paster)

    def set_image(self):
        self.imageview.configure(image=customtkinter.CTkImage(self.imager, size=(W*0.6,H*0.6)), require_redraw=True)

    def set_say(self):
        self.write_data({"chat": self.chat_text.get()})

    def set_next(self,frag):
        self.write_data({"next": frag})

# settings and frame for PicTextView
class PicTextViewFrame(BaseViewFrame):

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)

        self.title_text = customtkinter.StringVar(self, value="")

        self.text_frame = self.add_menu_options("Title")
        self.text_frame.grid_columnconfigure(0, weight=1)
        self.text_frame.grid_rowconfigure(0, weight=1)

        self.texter_frame = customtkinter.CTkFrame(self.text_frame)
        self.texter_frame.grid_columnconfigure((0,1,2), weight=1)
        self.texter_frame.grid_rowconfigure(0, weight=1)

        self.title = customtkinter.CTkEntry(self.texter_frame, textvariable=self.title_text, placeholder_text="Enter title here", width=300, height=20)
        self.title.grid(row=0, column=0, padx=10, pady=10)

        self.title_bold = customtkinter.CTkSwitch(self.texter_frame, text="Bold", command=self.set_textsettings)
        self.title_bold.grid(row=0, column=1, padx=5, pady=10)

        self.title_italic = customtkinter.CTkSwitch(self.texter_frame, text="Italic", command=self.set_textsettings)
        self.title_italic.grid(row=0, column=2, padx=5, pady=10)

        self.set_title = customtkinter.CTkButton(self.texter_frame, text="Set Title", command=self.set_text)
        self.set_title.grid(row=1, column=0, padx=10, pady=10)

        self.texter_frame.grid(row=0, column=0, padx=10, pady=10, sticky="new")

    def read_json(self, json, fragment_name):
        super().read_json(json, fragment_name)
        # set inital mode
        self.title_bold.deselect()
        self.title_italic.deselect()
        
        if self.fragmentjson.get("styles") != None:
            if self.fragmentjson["styles"] == 1:
                self.title_bold.select()         
            elif self.fragmentjson["styles"] == 2:
                self.title_italic.select()
            elif self.fragmentjson["styles"] == 3:
                self.title_bold.select()
                self.title_italic.select()

        if len(self.title_text.get()) > 0:
            self.title.delete(first_index=0, last_index=len(self.title_text.get())+1)
        if self.fragmentjson.get("title") != None:
            self.title.insert(0, self.fragmentjson["title"])

        self.update_image()
        self.set_image()

    def update_image(self):
        super().update_image()

        if self.fragmentjson.get("title") != None:
            fonttxt = ""
            if self.fragmentjson.get("textsettings") != None:
                fonttxt = text_styler[self.fragmentjson["textsettings"]]
            else:
                fonttxt = "Regular"

            fonter = ImageFont.truetype(os.path.join(os.getcwd(), "Roboto", f"Roboto-{fonttxt}"), fontsize)
            draw = ImageDraw.Draw(self.imager)

            txt = self.fragmentjson["title"]

            w, h = draw.textsize(txt, font=fonter)
            draw.text(((W-w)/2,(H-h)/2), txt, font=fonter, fill="black")

    def set_text(self):
        self.write_data({"title": self.title_text.get()})
        self.update_image()
        self.set_image()

    def set_textsettings(self):
        styles = 0
        styles += self.title_bold.get()
        styles += self.title_italic.get() * 2
        if styles > 0:
            self.write_data({"textsettings": styles})
        else:
            self.delete_data("textsettings")

        self.update_image()
        self.set_image()

# settings and frame for NavigationView
class NavigationViewFrame(PicTextViewFrame):

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)

        self.pointsjson = None

        # add new tab
        self.nav_frame = self.add_menu_options("Navigate")
        self.nav_frame.grid_columnconfigure((0,1), weight=1)

        # Location Choose frame
        self.location_frame = customtkinter.CTkFrame(self.nav_frame)
        self.location_frame.grid_columnconfigure(0, weight=1)

        self.location_label = customtkinter.CTkLabel(self.location_frame, text="Choose Location")
        self.location_label.grid(row=0, column=0, sticky="n", padx=10, pady=10)
        self.location_options = customtkinter.CTkOptionMenu(self.location_frame, values=None, command=self.set_location)
        self.location_options.grid(row=1, column=0, sticky="n", padx=10, pady=10)

        self.location_frame.grid(row=0, column=0, sticky="nsew", padx=10, pady=10)
        
        # Navigation Options Frame
        self.navmods_frame = customtkinter.CTkFrame(self.nav_frame)
        self.navmods_frame.grid_rowconfigure((0,1,2), weight=1)

        self.bgMove_switch = customtkinter.CTkSwitch(self.navmods_frame, text="Background Movement", command=self.set_navsettings)
        self.bgMove_switch.grid(row=0, column=0, sticky="nsew", padx=10)
        self.straight_switch = customtkinter.CTkSwitch(self.navmods_frame, text="GoTo Straight", command=self.set_navsettings)
        self.straight_switch.grid(row=1, column=0, sticky="nsew", padx=10)
        self.maxSpd_switch = customtkinter.CTkSwitch(self.navmods_frame, text="GoTo MaxSpeed", command=self.set_navsettings)
        self.maxSpd_switch.grid(row=2, column=0, sticky="nsew", padx=10)

        self.navmods_frame.grid(row=0, column=1, sticky="nsew", padx=10, pady=10)

    def read_json(self, json, fragment_name):
        super().read_json(json, fragment_name)

        self.bgMove_switch.deselect()
        self.straight_switch.deselect()
        self.maxSpd_switch.deselect()

        if self.pointsjson != None:
            if self.fragmentjson.get("location") != None:
                self.location_options.set(self.fragmentjson["location"])
                
                if self.fragmentjson.get("navsettings") != None:
                    navsettings = self.fragmentjson["navsettings"]
                    if navsettings["bgMove"]:
                        self.bgMove_switch.select()
                    if navsettings["straight"]:
                        self.straight_switch.select()
                    if navsettings["maxSpd"]:
                        self.maxSpd_switch.select()
            else:
                self.location_options.set("")

    def read_points(self,pointsjson):
        self.pointsjson = pointsjson
        if pointsjson != None:
            if len(pointsjson) > 0:
                self.location_options.configure(values=list(pointsjson.keys()))
            else:
                self.location_options.configure(values=[])
        else:
            self.location_options.configure(values=[])

    def set_location(self,location):
        self.write_data({"location":location})

    def set_navsettings(self):
        navdict = {"bgMove": bool(self.bgMove_switch.get()),
                    "straight": bool(self.straight_switch.get()), 
                    "maxSpd": bool(self.maxSpd_switch.get())
                    }
        
        if any(list(navdict.values())):
            self.write_data({"navsettings":navdict})
        else:
            self.delete_data("navsettings")

# settings for frame for MenuView
class MenuViewFrame(BaseViewFrame):

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)

        self.next_chooser.grid_forget()
        self.next_text.configure(text="Next fragment: NA")

        self.buttons_frame = self.add_menu_options("Buttons")
        self.buttons_frame.grid_rowconfigure(1, weight=1)
        self.buttons_frame.grid_columnconfigure((0,1), weight=1)
        self.view_buttons = {}

        self.numm_frame = customtkinter.CTkFrame(self.buttons_frame)
        self.numm_frame.grid_rowconfigure(1, weight=1)
        self.numm_frame.grid_columnconfigure((0,1), weight=1)

        self.num_labeller = customtkinter.CTkLabel(self.numm_frame, text="Number of buttons:")
        self.num_labeller.grid(row=0, column=0, sticky="new", padx=5, pady=5)

        self.num_buttons = customtkinter.CTkOptionMenu(self.numm_frame, values=[str(i) for i in range(1,7)], command=self.set_numbuttons)
        self.num_buttons.grid(row=0, column=1, sticky="new", padx=5, pady=5)

        self.numm_frame.grid(row=0, column=0, sticky="new", padx=10, pady=5)

        self.refresh_image_button = customtkinter.CTkButton(self.buttons_frame, text="Refresh Image", command=self.refresh_image)
        self.refresh_image_button.grid(row=0, column=1, sticky="nsew", padx=10, pady=5)

        self.buttons_edit = customtkinter.CTkTabview(self.buttons_frame)
        self.buttons_edit.grid(row=1, column=0, columnspan=2, sticky="nsew", padx=10)
        self.buttons_edit.grid_propagate(False)

    def add_button_menu(self, name):
        self.buttons_edit.add(name)
        return self.buttons_edit.tab(name)

    def read_json(self, json, fragment_name):
        
        # set inital variables
        self.number_buttons = 1
        self.num_buttons.set("1")
            
        # call BaseViewFrame read_json
        super().read_json(json, fragment_name)

        if self.fragmentjson.get("buttons") == None:
            self.fragmentjson["buttons"] = {}
        else:
            self.number_buttons = len(self.fragmentjson.get("buttons").keys())

        if self.fragmentjson.get("responses") == None:
            self.fragmentjson["responses"] = {}

        self.set_menu_buttons()
        self.refresh_image()

    def refresh_image(self):
        self.update_image()
        self.set_image()

    def set_menu_buttons(self):
        if self.view_buttons != {}:
            for i in self.view_buttons.keys():
                self.buttons_edit.delete(i)
        
        self.view_buttons = {}
        self.num_buttons.set(str(self.number_buttons))

        for i in range(self.number_buttons):
            b = f"button{i+1}"
            tts = self.add_button_menu(b)
            newview = ButtonViewFrame(tts)
            newview.pack(expand=True, anchor="center", fill="both")

            if self.fragmentjson["buttons"].get(b) == None:
                self.fragmentjson["buttons"].update({b: {"fragment": "home"}})

            if self.fragmentjson["responses"].get(b) == None:
                self.fragmentjson["responses"].update({b: []})

            newview.init(name=b, buttondict=self.fragmentjson["buttons"][b], responselist=self.fragmentjson["responses"][b])
            self.view_buttons.update({b: newview})

        # self.update_canvas()
        fragments = list(self.json["fragments"].keys())
        if self.json.get("start") != None:
            fragments.remove(self.json["start"])
            if self.json["start"] != self.fragmentname:
                fragments = ["home"] + fragments

        self.push_fragments(fragments)

        if len(self.fragmentjson["buttons"]) > self.number_buttons:
            for i in range(self.number_buttons+1, len(self.fragmentjson["buttons"])+1):
                del self.fragmentjson["buttons"][f"button{i}"]

    def update_image(self):
        super().update_image()
        if self.fragmentjson.get("buttons") != None:
            for b in self.fragmentjson["buttons"]:
                if self.fragmentjson["buttons"][b].get("image") != None:
                    if os.path.exists(os.path.join(images_path, self.fragmentjson["buttons"][b].get("image"))):
                        ss = Image.open(os.path.join(images_path, self.fragmentjson["buttons"][b]["image"])).convert("RGBA").resize((bW,bH))
                        b_img = Image.new("RGBA", (bW,bH), (255,255,255))
                        b_img.paste(ss,(0,0),ss)
                    else:
                        b_img = Image.new("RGBA", (bW, bH), (200, 200, 200))
                else:
                    b_img = Image.new("RGBA", (bW, bH), (200, 200, 200))

                self.number_buttons = len(self.fragmentjson["buttons"].keys())
                self.imager.paste(b_img, (int(coord_dict[self.number_buttons][b][0]), int(coord_dict[self.number_buttons][b][1])), b_img)

    def set_numbuttons(self,number):
        self.number_buttons = int(number)
        self.set_menu_buttons()
        self.refresh_image()

    def push_fragments(self, fragments):
        super().push_fragments(fragments)
        
        if len(fragments) != 0:
            for val in self.view_buttons.values():
                val.next_chooser.configure(values=fragments)

class ButtonViewFrame(customtkinter.CTkFrame):

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)

        self.grid_rowconfigure((0,1), weight=1)
        self.grid_columnconfigure(0, weight=1)

        self.next_frame = customtkinter.CTkFrame(self)
        self.next_frame.grid_rowconfigure(0, weight=1)
        self.next_frame.grid_columnconfigure((0,1), weight=1)
        self.next_text = customtkinter.CTkLabel(self.next_frame, text="Next fragment", anchor="center")
        self.next_text.grid(row=0, column=0, stick= "nsew", padx=5, pady=5)
        self.next_chooser = customtkinter.CTkOptionMenu(self.next_frame, values=[], command=self.set_next)
        self.next_chooser.grid(row=0, column=1, stick= "nsew", padx=5, pady=5)
        self.next_frame.grid(row=0, column=0, stick="ew", padx=10, pady=10)

        self.image_frame = customtkinter.CTkFrame(self)
        self.image_frame.grid_rowconfigure(0, weight=1)
        self.image_frame.grid_columnconfigure((0,1), weight=1)
        self.new_image = customtkinter.CTkButton(self.image_frame, text="Choose Image", anchor="center", command=self.choose_image)
        self.new_image.grid(row=0, column=0, sticky="nsew", padx=5, pady=5)
        self.current_image = customtkinter.CTkLabel(self.image_frame, text="Chosen image: None", anchor="w")
        self.current_image.grid(row=0, column=1, sticky="nsew", padx=5, pady=5)
        self.image_frame.grid(row=1, column=0, sticky="ew", padx=10, pady=10)

        self.listen_frame = customtkinter.CTkFrame(self)
        self.listen_frame.grid_rowconfigure(0, weight=1)
        self.listen_frame.grid_columnconfigure((0,1,2), weight=1)
        
        self.listen_phrase = customtkinter.StringVar()
        self.listen_phrase_box = customtkinter.CTkComboBox(self.listen_frame, variable=self.listen_phrase, values=[])
        self.listen_phrase_box.grid(row=0, column=0, sticky="nsew", padx=2, pady=2)

        self.add = customtkinter.CTkButton(self.listen_frame, text="Add Listen", command=self.adder)
        self.add.grid(row=1, column=0, sticky="ew", padx=5, pady=5)
        
        self.remove = customtkinter.CTkButton(self.listen_frame, text="Remove Listen", command=self.remover)
        self.remove.grid(row=2, column=0, sticky="ew", padx=5, pady=5)

        self.listen_frame.grid(row=0, column=1, rowspan=2, sticky="nsew", padx=10, pady=10)

    def init(self, name, buttondict, responselist):
        self.name = name
        # self.name_label.configure(text=self.name)
        self.buttondict = buttondict
        self.responselist = responselist

        if self.buttondict.get("fragment") != None:
            self.next_chooser.set(self.buttondict["fragment"])
        
        if self.buttondict.get("image") != None:
            self.new_image.configure(text="Remove Image")
            self.current_image.configure(text=f"Choosen image: {self.buttondict['image']}")

        if self.responselist != None:
            if len(self.buttondict) > 0:
                self.listen_phrase_box.configure(values=self.responselist)

    def write_data(self,data):
        self.buttondict.update(data)

    def delete_data(self,key):
        if self.buttondict.get(key) != None:
            del self.buttondict[key]

    def adder(self):
        if len(self.listen_phrase.get()) > 0 and not (self.listen_phrase.get() in self.responselist):
            self.responselist.append(self.listen_phrase.get())
            self.listen_phrase_box.configure(values=self.responselist)

    def remover(self):
        if len(self.listen_phrase.get()) > 0 and self.listen_phrase.get() in self.responselist:
            self.responselist.remove(self.listen_phrase.get())
            self.listen_phrase_box.configure(values=self.responselist)

    def choose_image(self):
        
        if self.new_image.cget("text") == "Choose Image":
            self.new_image.configure(text="Remove Image")

            fp = filedialog.askopenfilename(filetypes=[('Image', ('*.png', '*.jpg', '*.jpeg'))])
            if fp != "" and fp != None:
                f_name = str(fp).split("/")[-1]
                if not os.path.exists(images_path):
                    os.mkdir(images_path)
                try:
                    shutil.copy(fp, os.path.join(images_path, f_name))
                except shutil.SameFileError:
                    pass
                except Exception as e:
                    print(e)
                    pass
                self.write_data({"image": f_name})
            else:
                f_name = "None"

        else:
            f_name = "None"
            self.new_image.configure(text="Choose Image")
            self.delete_data("image")

        self.current_image.configure(text=f"Current image: {f_name}")

    def set_next(self,frag):
        self.write_data({"fragment": frag})
