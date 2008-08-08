die() {
  echo "$*"
  exit 1
}

test -z "$MAE_DIR" &&
  die "\$MAE_DIR should be set and point to a local MAE directory"
test -d "$MAE_DIR" ||
  die "\$MAE_DIR points to a non-existent directory $MAE_DIR"
echo "using local repository at $MAE_DIR"
