inherit linux-kernel-base deploy

DESCRIPTION = "QTI Video driver"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/GPL-2.0-only;md5=801f80980d171dd6425610833a22dbe6"

PR = "r0"

DEPENDS = "bc-native rsync-native mmrm-kernel"

do_configure[depends] += "virtual/kernel:do_shared_workdir"
do_compile[cleandirs] += "${WORKDIR}/out/${KERNEL_DEFCONFIG}"

FILESPATH =. "${WORKSPACE}:"
SRC_URI  =  "file://vendor/qcom/opensource/video-driver/"
SRC_URI +=  "file://${BASEMACHINE}/video_load.conf"

S = "${WORKDIR}/vendor/qcom/opensource/video-driver"
KERNEL_VERSION = "${@get_kernelversion_file("${STAGING_KERNEL_BUILDDIR}")}"
EXTRA_OEMAKE += "TARGET_SUPPORT=${BASEMACHINE}"

EXT_MODULES = "${@os.path.relpath("${S}", "${KERNEL_PLATFORM_PATH}")}"

do_compile() {
    cd ${KERNEL_PLATFORM_PATH}
    BUILD_CONFIG=msm-kernel/${KERNEL_CONFIG} \
    EXT_MODULES=${EXT_MODULES} \
    MODULE_OUT=${WORKDIR}/vendor/qcom/opensource/video-driver \
    KERNEL_KIT=${KERNEL_PREBUILT_PATH} \
    OUT_DIR=${WORKDIR}/out/${KERNEL_DEFCONFIG} \
    KERNEL_UAPI_HEADERS_DIR=${STAGING_KERNEL_BUILDDIR} \
    INSTALL_MODULE_HEADERS=1 \
    ./build/build_module.sh \
    VIDEO_ROOT=${WORKDIR}/vendor/qcom/opensource/video-driver \
    KBUILD_EXTRA_SYMBOLS=${STAGING_DIR_HOST}/lib/modules/${KERNEL_VERSION}/Module.symvers
}

do_install() {
    install -m 0644 ${WORKDIR}/${BASEMACHINE}/video_load.conf -D ${D}${sysconfdir}/modules-load.d/video_load.conf
    install -m 0644 ${WORKDIR}/vendor/qcom/opensource/video-driver/msm_video.ko -D ${D}${nonarch_base_libdir}/modules/${KERNEL_VERSION}/msm_video.ko
    install -m 0644 ${STAGING_KERNEL_BUILDDIR}/usr/include/vidc/media/v4l2_vidc_extensions.h -D ${D}/usr/include/vidc/media/v4l2_vidc_extensions.h
}

do_deploy() {
# Deploy unstripped kernel modules into ${DEPLOYDIR}/kernel_modules for debugging purposes
    install -d ${DEPLOYDIR}/kernel_modules
    for kmod in $(find ${D} -name "*.ko") ; do
        install -m 0644 $kmod ${DEPLOYDIR}/kernel_modules
    done
}

addtask deploy after do_install before do_package

FILES:${PN} += "${nonarch_base_libdir}/modules/${KERNEL_VERSION}/*"
