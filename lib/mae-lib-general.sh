die() {
  echo "$*"
  exit 1
}

filesize() {
  perl -e 'print -s $ARGV[0]' $1
}

test -z "$MAE_DIR" &&
  die "\$MAE_DIR should be set and point to a local MAE directory"
test -d "$MAE_DIR" ||
  die "\$MAE_DIR points to a non-existent directory $MAE_DIR"

bin="$base_dir/bin"
packs_dir="$MAE_DIR/packs"
catalog_dir="$MAE_DIR/catalog"

JAVAFLAGS="-Xmx128m -Xms128m"