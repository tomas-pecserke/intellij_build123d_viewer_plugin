from build123d import export_stl, BuildPart, Part, Shape, Solid
from os import path
from typing import Tuple, Union

def get_shape(obj):
    if isinstance(obj, Union[Part, Solid]):
        return obj
    if isinstance(obj, BuildPart):
        return obj.part
    return None

def grab_all_cad() -> set[Tuple[str, Shape]]:
    import inspect
    stack = inspect.stack()
    shapes = set()
    for frame in stack:
        for key, value in frame.frame.f_locals.items():
            shape = get_shape(value)
            if shape and shape not in shapes:
                shapes.add((key, shape))
    return shapes

for name, shape in grab_all_cad():
    export_stl(shape, path.join(tmp, f'{name}.stl'), ascii_format=True)
