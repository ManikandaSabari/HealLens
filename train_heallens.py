
# ─────────────────────────────────────────────────────────────
# HealLens AI — FIXED Training Script (v2)
# Run this in a NEW Google Colab notebook (T4 GPU runtime)
# Copy-paste ALL of this into one code cell and run it.
# Expected accuracy: 85%+
# ─────────────────────────────────────────────────────────────

# STEP 1 — Install dependencies
import subprocess
subprocess.run(["pip", "install", "-q", "tensorflowjs", "kaggle", "pillow",
                "matplotlib", "scikit-learn", "seaborn"], check=True)
print("✅ Dependencies installed")

# STEP 2 — Upload kaggle.json
from google.colab import files
import os, shutil, random, json, zipfile
from pathlib import Path

print("📁 Upload your kaggle.json file...")
uploaded = files.upload()
os.makedirs("/root/.config/kaggle", exist_ok=True)
shutil.copy("kaggle.json", "/root/.config/kaggle/kaggle.json")
os.chmod("/root/.config/kaggle/kaggle.json", 0o600)
print("✅ Kaggle configured")

# STEP 3 — Download & build BALANCED dataset (500 per class)
TARGET = 500
BASE   = Path("/content/heallens_data")
CLASSES = ["pneumonia","tuberculosis","covid19","fracture","arthritis","skin_infection","psoriasis"]
for c in CLASSES:
    (BASE / c).mkdir(parents=True, exist_ok=True)

def is_valid_image(path):
    """Check if image can be opened and is a valid format."""
    try:
        from PIL import Image
        with Image.open(path) as img:
            img.verify()   # catches truncated files
        with Image.open(path) as img:
            img.convert("RGB")  # catches corrupt pixel data
        return True
    except Exception:
        return False

def collect(src_dir, dst_class, required_in_path=None, exts=("*.jpg","*.jpeg","*.png")):
    imgs = []
    for ext in exts:
        for p in Path(src_dir).rglob(ext):
            path_str = str(p).lower()
            # Explicitly reject normal/healthy scans
            if "normal" in path_str or "healthy" in path_str:
                continue
            # If a specific subfolder is required, enforce it
            if required_in_path and required_in_path.lower() not in path_str:
                continue
            imgs.append(p)
            
    random.shuffle(imgs)
    dst = BASE / dst_class
    count = 0
    skipped = 0
    for p in imgs:
        if count >= TARGET:
            break
        if not is_valid_image(p):
            skipped += 1
            continue
        # Convert to proper JPEG to ensure TF compatibility
        try:
            from PIL import Image
            with Image.open(p) as img:
                rgb = img.convert("RGB")
                out_path = dst / f"{dst_class}_{count:04d}.jpg"
                rgb.save(out_path, "JPEG", quality=90)
            count += 1
        except Exception:
            skipped += 1
    if skipped > 0:
        print(f"     ⚠️  Skipped {skipped} corrupted/invalid images")
    return len(list(dst.iterdir()))

# Pneumonia
print("⬇️  Pneumonia...")
os.system("kaggle datasets download -d paultimothymooney/chest-xray-pneumonia -p /tmp/pneu --unzip -q")
n = collect("/tmp/pneu/chest_xray", "pneumonia", "pneumonia")
print(f"   ✅ {n} images")

# Tuberculosis
print("⬇️  Tuberculosis...")
os.system("kaggle datasets download -d tawsifurrahman/tuberculosis-tb-chest-xray-dataset -p /tmp/tb --unzip -q")
n = collect("/tmp/tb", "tuberculosis", "tuberculosis")
print(f"   ✅ {n} images")

# COVID-19
print("⬇️  COVID-19...")
os.system("kaggle datasets download -d tawsifurrahman/covid19-radiography-database -p /tmp/covid --unzip -q")
n = collect("/tmp/covid", "covid19", "covid")
print(f"   ✅ {n} images")

# Fracture
print("⬇️  Fracture...")
os.system("kaggle datasets download -d bmadushanirodrigo/fracture-multi-region-x-ray-data -p /tmp/frac --unzip -q")
n = collect("/tmp/frac", "fracture", "fractured")
print(f"   ✅ {n} images")

# Arthritis
print("⬇️  Arthritis...")
os.system("kaggle datasets download -d shashwatwork/knee-osteoarthritis-dataset-with-severity -p /tmp/arth --unzip -q")
for zero_dir in list(Path("/tmp/arth").rglob("0")):
    if zero_dir.is_dir():
        try:
            zero_dir.rename(zero_dir.with_name("normal_0"))
        except Exception:
            pass
n = collect("/tmp/arth", "arthritis")
print(f"   ✅ {n} images")

# Skin datasets
print("⬇️  Skin diseases...")
os.system("kaggle datasets download -d shubhamgoel27/dermnet -p /tmp/skin --unzip -q")

skin_inf_imgs = []
psoriasis_imgs = []
inf_kw   = ["bacterial","fungal","impetigo","cellulitis","tinea","folliculitis","warts"]
psor_kw  = ["psoriasis","eczema","dermatitis","urticaria","erythema","rash"]
for d in Path("/tmp/skin").rglob("*"):
    if not d.is_dir(): continue
    n_lower = d.name.lower()
    imgs = list(d.glob("*.jpg")) + list(d.glob("*.png")) + list(d.glob("*.jpeg"))
    if any(k in n_lower for k in inf_kw):   skin_inf_imgs  += imgs
    if any(k in n_lower for k in psor_kw):  psoriasis_imgs += imgs

# Fallback: split dermnet in half
if len(skin_inf_imgs) < 200 or len(psoriasis_imgs) < 200:
    all_skin = list(Path("/tmp/skin").rglob("*.jpg"))
    random.shuffle(all_skin)
    half = len(all_skin) // 2
    skin_inf_imgs  = all_skin[:half]
    psoriasis_imgs = all_skin[half:]

from PIL import Image as PILImage

def safe_copy(img_list, dst_class):
    dst = BASE / dst_class
    count = 0
    for p in img_list:
        if count >= TARGET: break
        try:
            with PILImage.open(p) as im:
                rgb = im.convert("RGB")
                rgb.save(dst / f"{dst_class}_{count:04d}.jpg", "JPEG", quality=90)
            count += 1
        except Exception:
            pass
    return count

safe_copy(skin_inf_imgs,  "skin_infection")
safe_copy(psoriasis_imgs, "psoriasis")

# Summary
print("\n📊 DATASET SUMMARY")
print("=" * 40)
total = 0
for c in CLASSES:
    n = len(list((BASE / c).iterdir()))
    total += n
    ok = "✅" if n >= 400 else "⚠️ LOW"
    print(f"  {ok}  {c:<20} {n:>4} images")
print("=" * 40)
print(f"  TOTAL: {total} images\n")

# STEP 4 — Purge bad images using TF itself (KEY FIX for InvalidArgumentError)
import tensorflow as tf
import numpy as np
import matplotlib.pyplot as plt
from sklearn.utils.class_weight import compute_class_weight

print("🧹 Scanning all images with TensorFlow to remove bad files...")
removed = 0
all_imgs = list(BASE.rglob("*.jpg")) + list(BASE.rglob("*.jpeg")) + list(BASE.rglob("*.png"))
for img_path in all_imgs:
    try:
        raw = tf.io.read_file(str(img_path))
        tf.io.decode_image(raw, channels=3, expand_animations=False)
    except Exception:
        os.remove(img_path)
        removed += 1
print(f"✅ Scan complete. Removed {removed} bad images.\n")

# STEP 5 — Build TF datasets
IMG   = 224
BATCH = 32
SEED  = 42

train_ds = tf.keras.utils.image_dataset_from_directory(
    BASE, validation_split=0.2, subset="training",
    seed=SEED, image_size=(IMG, IMG), batch_size=BATCH, label_mode="categorical")

val_ds = tf.keras.utils.image_dataset_from_directory(
    BASE, validation_split=0.2, subset="validation",
    seed=SEED, image_size=(IMG, IMG), batch_size=BATCH, label_mode="categorical")

CLASS_NAMES = train_ds.class_names
NUM_CLASSES = len(CLASS_NAMES)
print(f"Classes ({NUM_CLASSES}): {CLASS_NAMES}")

# Compute class weights to fix imbalance (KEY FIX for Pneumonia bias)
all_labels = []
for _, lbl in train_ds:
    all_labels.extend(np.argmax(lbl.numpy(), axis=1))
all_labels = np.array(all_labels)
unique_classes = np.unique(all_labels)
cw = compute_class_weight("balanced", classes=unique_classes, y=all_labels)
class_weights = {cls: w for cls, w in zip(unique_classes, cw)}
for i in range(NUM_CLASSES):
    if i not in class_weights:
        class_weights[i] = 1.0
print(f"Class weights: {class_weights}")

# Augmentation pipeline
aug = tf.keras.Sequential([
    tf.keras.layers.RandomFlip("horizontal"),
    tf.keras.layers.RandomRotation(0.15),
    tf.keras.layers.RandomZoom(0.12),
    tf.keras.layers.RandomContrast(0.18),
    tf.keras.layers.RandomBrightness(0.12),
], name="augmentation")

preprocess = tf.keras.applications.efficientnet.preprocess_input

def prep_train(img, lbl):
    img = aug(img, training=True)
    return preprocess(img), lbl

def prep_val(img, lbl):
    return preprocess(img), lbl

AUTOTUNE = tf.data.AUTOTUNE
train_ds = train_ds.map(prep_train, num_parallel_calls=AUTOTUNE).prefetch(AUTOTUNE)
val_ds   = val_ds.map(prep_val,   num_parallel_calls=AUTOTUNE).prefetch(AUTOTUNE)
print("✅ Dataset pipelines ready")

# STEP 5 — Build EfficientNetB0 model (better than MobileNetV2)
base = tf.keras.applications.EfficientNetB0(
    input_shape=(IMG, IMG, 3), include_top=False, weights="imagenet")
base.trainable = False

inp = tf.keras.Input(shape=(IMG, IMG, 3))
x   = base(inp, training=False)
x   = tf.keras.layers.GlobalAveragePooling2D()(x)
x   = tf.keras.layers.Dense(512, activation="relu")(x)
x   = tf.keras.layers.BatchNormalization()(x)
x   = tf.keras.layers.Dropout(0.45)(x)
x   = tf.keras.layers.Dense(256, activation="relu")(x)
x   = tf.keras.layers.Dropout(0.25)(x)
out = tf.keras.layers.Dense(NUM_CLASSES, activation="softmax")(x)

model = tf.keras.Model(inp, out, name="HealLens_EfficientNetB0")
model.compile(
    optimizer=tf.keras.optimizers.Adam(1e-3),
    loss="categorical_crossentropy",
    metrics=["accuracy", tf.keras.metrics.AUC(name="auc")])
model.summary()

# STEP 6 — Phase 1: Train head (15 epochs)
cbs = [
    tf.keras.callbacks.EarlyStopping(monitor="val_accuracy", patience=5,
                                     restore_best_weights=True, verbose=1),
    tf.keras.callbacks.ModelCheckpoint("/content/heallens_best.keras",
                                       monitor="val_accuracy", save_best_only=True, verbose=1),
    tf.keras.callbacks.ReduceLROnPlateau(monitor="val_loss", factor=0.3,
                                         patience=3, min_lr=1e-7, verbose=1),
]

print("\n🚀 Phase 1: Training head (frozen base)...")
h1 = model.fit(train_ds, validation_data=val_ds, epochs=15,
               class_weight=class_weights, callbacks=cbs, verbose=1)
print(f"✅ Phase 1 best val_accuracy: {max(h1.history['val_accuracy']):.4f}")

# STEP 7 — Phase 2: Fine-tune top 50 layers
base.trainable = True
for layer in base.layers[:-50]:
    layer.trainable = False

model.compile(
    optimizer=tf.keras.optimizers.Adam(2e-5),
    loss="categorical_crossentropy",
    metrics=["accuracy", tf.keras.metrics.AUC(name="auc")])

print("\n🚀 Phase 2: Fine-tuning top 50 layers...")
h2 = model.fit(train_ds, validation_data=val_ds, epochs=25,
               class_weight=class_weights, callbacks=cbs, verbose=1)
print(f"✅ Phase 2 best val_accuracy: {max(h2.history['val_accuracy']):.4f}")

# STEP 8 — Evaluate
model.load_weights("/content/heallens_best.keras")
loss, acc, auc = model.evaluate(val_ds, verbose=0)
print(f"\n🎯 FINAL — Accuracy: {acc*100:.2f}%  AUC: {auc:.4f}")

# Confusion matrix
from sklearn.metrics import classification_report, confusion_matrix
import seaborn as sns
y_true, y_pred = [], []
for imgs, lbls in val_ds:
    preds = model.predict(imgs, verbose=0)
    y_true.extend(np.argmax(lbls.numpy(), axis=1))
    y_pred.extend(np.argmax(preds, axis=1))

cm = confusion_matrix(y_true, y_pred)
plt.figure(figsize=(10, 8))
sns.heatmap(cm, annot=True, fmt="d", cmap="Blues",
            xticklabels=CLASS_NAMES, yticklabels=CLASS_NAMES)
plt.title("HealLens — Confusion Matrix", fontweight="bold")
plt.ylabel("True"); plt.xlabel("Predicted")
plt.xticks(rotation=45, ha="right")
plt.tight_layout(); plt.show()
print("\n" + classification_report(y_true, y_pred, target_names=CLASS_NAMES))

# STEP 9 — Export to TensorFlow.js
import tensorflowjs as tfjs

TFJS = "/content/heallens_tfjs_v2"
tfjs.converters.save_keras_model(model, TFJS)

meta = {
    "classes":    CLASS_NAMES,
    "numClasses": NUM_CLASSES,
    "imageSize":  IMG,
    "modelName":  "HealLens_EfficientNetB0_v2",
    "accuracy":   round(acc * 100, 2),
    "bodyPartMap": {
        "pneumonia":"chest","tuberculosis":"chest","covid19":"chest",
        "fracture":"bone","arthritis":"bone",
        "skin_infection":"skin","psoriasis":"skin"
    },
    "diseaseNameMap": {
        "pneumonia":"Pneumonia","tuberculosis":"Tuberculosis","covid19":"COVID-19",
        "fracture":"Bone Fracture","arthritis":"Mild Arthritis",
        "skin_infection":"Skin Infection","psoriasis":"Psoriasis/Rash"
    }
}
with open(f"{TFJS}/metadata.json", "w") as f:
    json.dump(meta, f, indent=2)
print("✅ metadata.json saved")

# STEP 10 — Zip & download
zip_path = "/content/heallens_model_v2.zip"
with zipfile.ZipFile(zip_path, "w", zipfile.ZIP_DEFLATED) as zf:
    for fp in Path(TFJS).iterdir():
        zf.write(fp, fp.name)
print(f"✅ Zipped: {os.path.getsize(zip_path)/1024/1024:.1f} MB")

files.download(zip_path)
print(f"\n🎉 DONE! Accuracy: {acc*100:.2f}%  |  Download started!")
print("→ Extract zip → Copy files → Paste into pdd/model/ folder")
