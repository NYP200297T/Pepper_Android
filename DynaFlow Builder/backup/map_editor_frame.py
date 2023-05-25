import customtkinter
import math
from PIL import Image
import os
from typing import Union
from fragment_frames import SkeletonFrame

class MapEditorFrame(SkeletonFrame):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.pointsjson = None
        self.adb_framer = None

        self.map_dir = customtkinter.StringVar(value="/Maps/")
        self.map_json = customtkinter.StringVar(value="mapData.txt")
        self.points_dir = customtkinter.StringVar(value="/DynaFlow/")
        self.points_json = customtkinter.StringVar(value="points.json")
        
        self.map_image = Image.open(os.path.join(os.getcwd(), "Maps", "map.png"))
        self.map_image_ctk = customtkinter.CTkImage(self.map_image, size=self.map_image.size)

        self.map_canvas = customtkinter.CTkCanvas(self, width=self.map_image.size[0], height=self.map_image.size[1])
        self.map_canvas.create_image((0,0), image=self.map_image_ctk, anchor="center")
        self.map_canvas.grid(row=0, column=0, sticky="nsew")

        self.settings_frame = self.add_menu_options("Settings")

        self.switch_default = customtkinter.CTkSwitch(self.settings_frame, text="Use default directories", command=self.set_default_dir, onvalue=True, offvalue=False)
        self.switch_default.grid(row=0, column=0, sticky="nsew")

        self.custom_dir_frame = customtkinter.CTkFrame(self.settings_frame)
        
        self.entry_map_dir = customtkinter.CTkEntry(self.custom_dir_frame, textvariable=self.map_dir)
        self.entry_map_dir.pack(expand=True, fill='x', anchor="center", pady=5, padx=5)
        self.entry_map_json = customtkinter.CTkEntry(self.custom_dir_frame, textvariable=self.map_json)
        self.entry_map_json.pack(expand=True, fill='x', anchor="center", pady=5, padx=5)
        self.entry_points_dir = customtkinter.CTkEntry(self.custom_dir_frame, textvariable=self.points_dir)
        self.entry_points_dir.pack(expand=True, fill='x', anchor="center", pady=5, padx=5)
        self.entry_points_json = customtkinter.CTkEntry(self.custom_dir_frame, textvariable=self.points_json)
        self.entry_points_json.pack(expand=True, fill='x', anchor="center", pady=5, padx=5)
        
        self.custom_dir_frame.grid(row=1, column=0, sticky="nsew")
        
        self.mapdata_button = customtkinter.CTkButton(self.settings_frame, command=self.set_map_data)
        self.mapdata_button.grid(row=2, column=0, sticky="nsew")

        self.switch_default.select()
        self.set_default_dir()
        self.load_points()

    def read_json(self, json, pointsjson):
        self.json = json
        self.pointsjson = pointsjson

    def load_points(self):
        for point in self.pointsjson:
            new_point = self.add_menu_options(point)
            new_point_frame = PointSetFrame(new_point)
            new_point_frame.load_data(self.points_json[point])
            self.draw_point

    def start_point(self):
        self.map_canvas.bind('<Button-1>', self.set_point)

    def draw_point(self, x:Union[int,float], y:Union[int,float], theta:float=None):
        x = int(x)
        y = int(y)
        self.map_canvas.create_oval(x,y,x,y, fill="red", width=5)

    def set_point(self,event):
        self.draw_point(x=event.x, y=event.y)        
        self.map_canvas.unbind_all()

    def load_adbframe(self, adb_framer):
        self.adb_framer = adb_framer

    def set_default_dir(self):
        if self.switch_default.get():
            for element in list(self.custom_dir_frame.children.values()):
                element.configure(state="disabled")
        else:
            for element in list(self.custom_dir_frame.children.values()):
                element.configure(state="normal")

    def set_map_data(self):
        if self.switch_default.get():
            mapdict = {
                "directory": "/Maps/",
                "map": "mapData.txt",
                "points_directory": "/DynaFlow/",
                "points": "points.json"
                }
        else:
            mapdict = {
                "directory": self.map_dir.get(),
                "map": self.map_json.get(),
                "points_directory": self.points_dir.get(),
                "points": self.points_json.get()
                }
        self.json.update({"mapSettings": mapdict})

class PointSetFrame(customtkinter.CTkFrame):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.data = None

    def load_data(self, pointjson):
        self.data = pointjson
