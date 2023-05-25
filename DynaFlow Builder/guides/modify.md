### Adding new fragments
To add new fragment builders, create a class that extends from `BaseViewFrame` in [fragment_frames.py](../fragment_frames.py)

This class provides a `customtkinter.CTkLabel` named `self.imager` to show current screen and a `customtkinter.CTkTabview` for settings.

`BaseViewFrame` provides a General settings screen with `Say`, `Next fragment` and `Background Image` settings.

To read addtional settings for your custom fragment, override `read_json` method with a `super()` call.

Here is an example:
```py
class CustomFragment(BaseViewFrame):

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)

        # setup your frames and UI elements here
    
    def read_json(self, json, fragment_name):
        super().read_json(json, fragment_name)
        # read from self.fragmentjson or json
        # add rest of read here
```

And modify [app.py](../app.py) with the following:
```py
from fragment_frames import ... CustomFragment

class App(cutomtkinter.CTk):

    ...

    def init(self):
        ...
        # after line 130
        self.custom_framer = CustomFragment(self.controlpanel_frame)
        ...

    def update_view_selection(self,name):
        ...
        # assuming type is customview
        if view_type == "customview":
            self.read_json(self.jsondict, name)
            self.settings_frame.pack(anchor="center", expand=True, fill="both")
            self.current_viewer = self.custom_framer
        ...
```