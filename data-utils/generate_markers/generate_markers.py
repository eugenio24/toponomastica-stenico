from PIL import Image, ImageDraw, ImageFont
import os
import json
import subprocess

INPUT_JSON = '../../data/toponimi/Stenico.json'
OUT_DIR = "output"

os.makedirs(f"{OUT_DIR}/unselected", exist_ok=True)
os.makedirs(f"{OUT_DIR}/selected", exist_ok=True)

def create_marker(text, base_color, text_color, output_path, border_color=None):
    font_path = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"
    font_size = 24
    font = ImageFont.truetype(font_path, font_size)

    # Temporary image to measure text bbox
    temp_img = Image.new("RGBA", (1, 1))
    draw = ImageDraw.Draw(temp_img)

    bbox = draw.textbbox((0, 0), text, font=font)
    text_width = bbox[2] - bbox[0]
    text_height = bbox[3] - bbox[1]

    padding_x = 20
    padding_y = 10
    pointer_height = 10

    width = text_width + 2 * padding_x
    height = text_height + 2 * padding_y + pointer_height

    img = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # Draw rounded rectangle with optional border
    radius = 12
    rect = [0, 0, width, height - pointer_height]
    if border_color:
        border_thickness = 3
        # Outer rectangle - border
        draw.rounded_rectangle(rect, radius, fill=border_color)
        # Inner rectangle - background
        inner_rect = [
            border_thickness,
            border_thickness,
            width - border_thickness,
            height - pointer_height - border_thickness
        ]
        draw.rounded_rectangle(inner_rect, radius - border_thickness, fill=base_color)
    else:
        draw.rounded_rectangle(rect, radius, fill=base_color)

    # Draw pointer triangle (V shape)
    triangle = [
        (width // 2 - pointer_height, height - pointer_height),
        (width // 2 + pointer_height, height - pointer_height),
        (width // 2, height)
    ]

    if border_color:
        # Border triangle (slightly bigger)
        border_triangle = [
            (width // 2 - pointer_height - 2, height - pointer_height),
            (width // 2 + pointer_height + 2, height - pointer_height),
            (width // 2, height + 2)
        ]
        draw.polygon(border_triangle, fill=border_color)
        draw.polygon(triangle, fill=base_color)
    else:
        draw.polygon(triangle, fill=base_color)

    # Draw text centered horizontally and vertically inside rectangle
    text_x = width / 2
    text_y = (height - pointer_height) / 2
    draw.text((text_x, text_y), text, font=font, fill=text_color, anchor="mm")

    img.save(output_path)

def pngquant_compress(input_path):
    """Try to compress PNG with pngquant, overwrite the original."""
    try:
        subprocess.run([
            "pngquant", "--quality=65-80", "--speed=1", "--force", "--output", input_path, input_path
        ], check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        print(f"Compressed {input_path} with pngquant")
    except FileNotFoundError:
        print("pngquant not found, skipping compression.")
    except subprocess.CalledProcessError as e:
        print(f"pngquant compression failed on {input_path}: {e}")

def main():
    with open(INPUT_JSON, "r", encoding="utf-8") as f:
        toponyms = json.load(f)

    # Create markers
    for t in toponyms:
        name = t["nome"]
        id_ = t["id"]

        unselected_path = f"{OUT_DIR}/unselected/marker_{id_}.png"
        selected_path = f"{OUT_DIR}/selected/marker_{id_}.png"

        # Unselected
        create_marker(name, base_color="#6750A4", text_color="white",
                      output_path=unselected_path)

        # Selected
        create_marker(name, base_color="white", text_color="#6750A4", border_color="#6750A4",
                      output_path=selected_path)

    # Compress with pngquant
    for state in ["unselected", "selected"]:
        out_dir = f"{OUT_DIR}/{state}"
        for filename in os.listdir(out_dir):
            if filename.endswith(".png"):
                pngquant_compress(os.path.join(out_dir, filename))

if __name__ == "__main__":
    main()
