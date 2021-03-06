# RPM Metadata
%define _component_name           awips2-cave-wrapper
%define _component_project_dir    awips2.cave/Installer.cave-wrapper
%define _component_zip_file_name  CAVE-linux.gtk.x86_64.zip
#
# awips2-cave-wrapper Spec File
#
%define __prelink_undo_cmd %{nil}
# Disable the brp-python-bytecompile script
%global __os_install_post %(echo '%{__os_install_post}' | sed -e 's!/usr/lib[^[:space:]]*/brp-python-bytecompile[[:space:]].*$!!g')
# Disable the rpm jar repack script
%global __os_install_post %(echo '%{__os_install_post}' | sed -e 's!/usr/lib[^[:space:]]*/brp-java-repack-jars[[:space:]].*$!!g')

Name: %{_component_name}
Summary: awips2-cave-wrapper Installation
Version: %{_component_version}
Release: %{_component_release}%{?dist}
Group: AWIPSII
BuildRoot: /tmp
URL: N/A
License: N/A
Distribution: N/A
Vendor: %{_build_vendor}
Packager: %{_build_site}

AutoReq: no
Provides: %{_component_name}
Requires: awips2-cave

BuildRequires: awips2-java

%description
Provides scripts and other Viz configuration that make starting CAVE easier.

%prep
# Verify That The User Has Specified A BuildRoot.
if [ "${RPM_BUILD_ROOT}" = "/tmp" ]
then
   echo "An Actual BuildRoot Must Be Specified. Use The --buildroot Parameter."
   echo "Unable To Continue ... Terminating"
   exit 1
fi
if [ -d ${RPM_BUILD_ROOT} ]; then
   rm -rf ${RPM_BUILD_ROOT}
fi
CAVE_DIST_DIR="%{_baseline_workspace}/rpms/awips2.cave/setup/dist"

if [ ! -f ${CAVE_DIST_DIR}/%{_component_zip_file_name} ]; then
   echo "ERROR: Unable to find - ${CAVE_DIST_DIR}/%{_component_zip_file_name}."
   exit 1
fi

%build
pushd . > /dev/null 2>&1
cd %{_baseline_workspace}/rpms/awips2.cave/Installer.cave-wrapper/scripts/memory/iniFileGenerator/src
/awips2/java/bin/javac main/*
if [ $? -ne 0 ]; then
   exit 1
fi
popd > /dev/null 2>&1

%install
mkdir -p ${RPM_BUILD_ROOT}/awips2/cave
if [ $? -ne 0 ]; then
   exit 1
fi
mkdir -p ${RPM_BUILD_ROOT}/usr/share/applications
if [ $? -ne 0 ]; then
   exit 1
fi

# The AWIPS II version script.
VERSIONS_SCRIPT="%{_baseline_workspace}/rpms/utility/scripts/versions.sh"
cp ${VERSIONS_SCRIPT} ${RPM_BUILD_ROOT}/awips2/cave
if [ $? -ne 0 ]; then
   exit 1
fi

# testWS script
TEXTWS_SCRIPT="%{_baseline_workspace}/rpms/utility/scripts/textWS.sh"
cp ${TEXTWS_SCRIPT} ${RPM_BUILD_ROOT}/awips2/cave
if [ $? -ne 0 ]; then
   exit 1
fi

# text-workstation autostart script.
CAVE_SCRIPTS_DIR="%{_baseline_workspace}/rpms/%{_component_project_dir}/scripts"
cp ${CAVE_SCRIPTS_DIR}/autostart/cave.desktop ${RPM_BUILD_ROOT}/usr/share/applications/
if [ $? -ne 0 ]; then
   exit 1
fi

# CAVE scripts and other required distributables.
_build_cave_static="%{_baseline_workspace}/build/static"
# we want the common directory, the common linux directory, and the architecture-specific Linux directory.
_common_dir="${_build_cave_static}/common/cave"
_linux_dir="${_build_cave_static}/linux/cave"
_linux_arch_dir="${_build_cave_static}/linux.x86_64/cave"

cp -rv ${_common_dir}/* ${RPM_BUILD_ROOT}/awips2/cave
if [ $? -ne 0 ]; then
   exit 1
fi
cp -rv ${_linux_dir}/* ${RPM_BUILD_ROOT}/awips2/cave
if [ $? -ne 0 ]; then
   exit 1
fi
cp -rv ${_linux_arch_dir}/* ${RPM_BUILD_ROOT}/awips2/cave
if [ $? -ne 0 ]; then
   exit 1
fi

CAVE_DIST_DIR="%{_baseline_workspace}/rpms/awips2.cave/setup/dist"
_cave_zip=${CAVE_DIST_DIR}/%{_component_zip_file_name}
_mem_settings_xml=%{_baseline_workspace}/rpms/awips2.cave/Installer.cave-wrapper/scripts/memory/memorySettings.xml
_ini_destination=${RPM_BUILD_ROOT}/awips2/cave

pushd . > /dev/null 2>&1
cd %{_baseline_workspace}/rpms/awips2.cave/Installer.cave-wrapper/scripts/memory/iniFileGenerator/src
/awips2/java/bin/java main/IniFileGenerator "${_cave_zip}" "${_mem_settings_xml}" "${_ini_destination}"
if [ $? -ne 0 ]; then
   exit 1
fi
rm -fv main/*.class
if [ $? -ne 0 ]; then
   exit 1
fi
popd > /dev/null 2>&1

pushd . > /dev/null 2>&1
cd %{_baseline_workspace}/viz.updater
if [ ! -d bin ]; then
   mkdir -p bin
fi
/awips2/ant/bin/ant -Ddest.dir=${RPM_BUILD_ROOT}/awips2/cave \
   -Declipse.dir=%{_uframe_eclipse} -Dbaseline.dir=%{_baseline_workspace} -f build.xml
if [ $? -ne 0 ]; then
   exit 1
fi
popd > /dev/null 2>&1

%pre
%post
%preun
%postun

%clean
rm -rf ${RPM_BUILD_ROOT}

%files
%defattr(644,awips,fxalpha,755)
%dir /awips2/cave
/awips2/cave/*.ini
%dir /awips2/cave/etc
/awips2/cave/etc/*

%defattr(755,awips,fxalpha,755)
%dir /awips2/cave/caveEnvironment
/awips2/cave/caveEnvironment/*
/awips2/cave/*.sh
/awips2/cave/cave.png
/awips2/cave/VizUpdater.jar
# not a noarch RPM due to the presence of the architecture-specific libraries.
%dir /awips2/cave/lib64
/awips2/cave/lib64/*

%attr(644,root,root) /usr/share/applications/cave.desktop
