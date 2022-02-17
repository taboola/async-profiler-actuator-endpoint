#!/bin/sh
echo "##### Starting bootstrap script #####"

export LANG=en_US.UTF-8
export LC_ALL=$LANG

assets_suffixes=("linux-arm64.tar.gz" "linux-x64.tar.gz" "macos.zip")
asset_name_prefix="async-profiler-$1-"
asset_url_prefix="https://github.com/jvm-profiling-tools/async-profiler/releases/download/v$1/$asset_name_prefix"
binaries_dir="bin/"

echo "Downloading missing assets"
for asset_suffix in "${assets_suffixes[@]}"
do
  asset_name_without_suffix=`echo $asset_suffix | awk -F. '{print $1}'`
  target_lib_dir="src/main/resources/async-profiler-libs/$asset_name_without_suffix/"
  target_lib_file="$target_lib_dir""libasyncProfiler.so"

  if [ -f "$target_lib_file" ]; then
      echo "Lib '$target_lib_file' exists, skipping"
  else
      echo "Lib '$target_lib_file' does not exist, getting it"

      url="$asset_url_prefix$asset_suffix"
      asset_full_name="$asset_name_prefix$asset_suffix"
      download_dir="$binaries_dir$asset_name_without_suffix/"
      download_file="$download_dir$asset_full_name"

      if [ -f "$download_file" ]; then
        echo "Removing $download_file"
        rm "$download_file"
      fi

      echo "Downloading $asset_full_name from $url"
      wget $url -P "$download_dir"

      echo "Unpacking $download_file"
      if [[ $download_file == *.zip ]]; then
        unzip $download_file '-d' $download_dir
      else
        tar '-xvzf' $download_file '-C' $download_dir
      fi

      source_lib_file="$download_dir""async-profiler-$1-$asset_name_without_suffix/build/libasyncProfiler.so"
      echo "Copying $source_lib_file to $target_lib_dir"
      mkdir -p $target_lib_dir && cp $source_lib_file $target_lib_dir
  fi
done

echo "Removing temporary 'bin/' directory if exists"
rm '-rf' 'bin/'

echo "##### Finished running bootstrap script #####"