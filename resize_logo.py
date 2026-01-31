import os
from PIL import Image

def resize_image(input_path, output_dir, sizes):
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)
    
    img = Image.open(input_path)
    
    # Crop to square if not already
    width, height = img.size
    min_dim = min(width, height)
    left = (width - min_dim) / 2
    top = (height - min_dim) / 2
    right = (width + min_dim) / 2
    bottom = (height + min_dim) / 2
    img = img.crop((left, top, right, bottom))

    for name, size in sizes.items():
        folder = os.path.join(output_dir, f"mipmap-{name}")
        if not os.path.exists(folder):
            os.makedirs(folder)
        
        resized_img = img.resize((size, size), Image.Resampling.LANCZOS)
        resized_img.save(os.path.join(folder, "ic_launcher.png"))
        resized_img.save(os.path.join(folder, "ic_launcher_round.png"))

if __name__ == "__main__":
    # Standard Android Icon Sizes
    # mdpi: 48x48
    # hdpi: 72x72
    # xhdpi: 96x96
    # xxhdpi: 144x144
    # xxxhdpi: 192x192
    icon_sizes = {
        "mdpi": 48,
        "hdpi": 72,
        "xhdpi": 96,
        "xxhdpi": 144,
        "xxxhdpi": 192
    }
    
    input_logo = "/home/ubuntu/SpidyEngine/logos/logo_raw.png"
    output_res = "/home/ubuntu/SpidyEngine/app/src/main/res"
    
    resize_image(input_logo, output_res, icon_sizes)
    print("Logo resizing complete.")
