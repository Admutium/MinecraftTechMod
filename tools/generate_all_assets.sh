#!/usr/bin/env bash
set -euo pipefail
repo_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
python3 "$repo_dir/tools/generate_assets.py"
python3 "$repo_dir/tools/generate_machines.py"
python3 "$repo_dir/tools/generate_crushed_ores.py"
python3 "$repo_dir/tools/generate_solar.py"
python3 "$repo_dir/tools/generate_conveyors.py"
python3 "$repo_dir/tools/generate_materials.py"
python3 "$repo_dir/tools/generate_update_models.py"
python3 "$repo_dir/tools/generate_sounds.py"
