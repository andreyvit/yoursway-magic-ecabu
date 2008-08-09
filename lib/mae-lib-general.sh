die() {
  echo -e "$*"
  exit 1
}

filesize() {
  perl -e 'print -s $ARGV[0]' $1
}

check_version() {
  perl -e 'exit ($ARGV[0] =~ /^[a-z0-9.-]+\/[a-z0-9.-]+\/[a-z0-9.-]+$/ ? 0 : 1)' "$1"
}

explode_version() {
  perl -e '$ARGV[0] =~ /^([a-z0-9.-]+)\/([a-z0-9.-]+)\/([a-z0-9.-]+)$/; print "$1 $2 $3"' "$1"
}

flatten_version() {
  echo "${1//\//_}"
}

test -z "$MAE_DIR" &&
  die "\$MAE_DIR should be set and point to a local MAE directory"
test -d "$MAE_DIR" ||
  die "\$MAE_DIR points to a non-existent directory $MAE_DIR"

bin="$base_dir/bin"
packs_dir="$MAE_DIR/packs"
catalog_dir="$MAE_DIR/catalog"
tree_dir="$MAE_DIR/trees"
component_dir="$MAE_DIR/components"
product_dir="$MAE_DIR/products"

JAVAFLAGS="-Xmx128m -Xms128m"

tab="$(echo -e "\t")"
