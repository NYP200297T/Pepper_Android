from typing import Union
import customtkinter
import math
import json
from PIL import Image
import os

class TestApp(customtkinter.CTk):

    def __init__(self):
        super().__init__()

        self.map_img = Image.open(fp=os.path.join(os.getcwd(), "Maps", "map.png"))
        img_width, img_height = self.map_img.size
        self.map_img_ctk = customtkinter.CTkImage(self.map_img, size=self.map_img.size)

        self.map_canvas = customtkinter.CTkCanvas(self, width=img_width, height=img_height)
        self.map_canvas.pack()

        self.framer = customtkinter.CTkFrame(self.map_canvas)
        self.label = customtkinter.CTkLabel(master=self.framer, text="", image=self.map_img_ctk)
        self.label.pack(expand=True, fill='both', anchor="center")

        # self.map_canvas.create_window((0,0), window=self.framer, width=img_width, height=img_height, anchor='nw')
        self.read_json()

    def read_json(self):
        with open(os.path.join(os.getcwd(), "Maps", "points.json"), errors="ignore") as json_r:
            json_str = json_r.read()
            if not json_str.startswith("{"):
                partitioner = json_str.partition('"')
                rejson_str = "".join(partitioner[1:])
                rejson_str = '{'+rejson_str
                self.pointsjson = json.loads(rejson_str)
            else:
                self.pointsjson = json.load(json_r)

        with open(os.path.join(os.getcwd(), "Maps", "mapinfo.json")) as mapinfo_r:
            self.mapinfo = json.load(mapinfo_r)

        self.load_points()

    def load_points(self):
        for point in self.pointsjson:
            pointdict = self.pointsjson[point]
            x,y = self.map_to_px(pointdict["x"], pointdict["y"], pointdict["theta"])
            print(x,y)
            self.draw_point(x,y)

    def map_to_px(self, xMap:float, yMap:float, theta:float):
        scale = self.mapinfo["scale"]
        x = self.mapinfo["x"]
        y = self.mapinfo["y"]

        xPixel = (1 / scale * (math.cos(theta) * (xMap - x) + math.sin(theta) * (yMap - y)))
        yPixel = (1 / scale * (math.sin(theta) * (xMap - x) - math.cos(theta) * (yMap - y)))

        return (xPixel, yPixel)

    def px_to_map(self, xPixel:float, yPixel:float, theta:float):
        scale = self.mapinfo["scale"]
        x = 0 #self.mapinfo["x"]
        y = 0 #self.mapinfo["y"]

        xMap = scale * (math.cos(theta) * xPixel + math.sin(theta) * yPixel) + x
        yMap = scale * (math.cos(theta) * xPixel - math.sin(theta) * yPixel) + y

        return (xMap, yMap)

    def start_point(self):
        self.map_canvas.bind('<Button-1>', self.set_point)

    def draw_point(self, x:float, y:float, theta:float=None):
        print("drawing")
        self.map_canvas.create_oval(x-5,y-5,x+5,y+5, fill="red")

    def set_point(self,event):
        self.draw_point(x=event.x, y=event.y)        
        self.map_canvas.unbind_all()

if __name__ == "__main__":
    app = TestApp()
    app.mainloop()