inherit linux-kernel-base deploy

DESCRIPTION = "QTI Video driver"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/GPL-2.0-only;md5=801f80980d171dd6425610833a22dbe6"

FILESEXTRAPATHS:prepend := "${WORKSPACE}:"
SRC_URI  =  "file://vendor/qcom/opensource/video-driver/"
SRC_URI +=  "file://${BASEMACHINE}/video_load.conf"
S = "${WORKDIR}/vendor/qcom/opensource/video-driver"
DEPENDS += "virtual/kernel bc-native rsync-native synx-kernel-header mmdlkm-headers"

KERNEL_VERSION = "${@get_kernelversion_file("${STAGING_KERNEL_BUILDDIR}")}"
EXT_MODULES = "${@os.path.relpath("${S}", "${KERNEL_PLATFORM_PATH}")}"
INTERMEDIATE_KERNEL_PATH = "${WORKDIR}/out/${KERNEL_DEFCONFIG}"

do_configure[noexec] = "1"

do_compile[depends]   += "virtual/kernel:do_shared_workdir"
do_compile[cleandirs] += "${INTERMEDIATE_KERNEL_PATH}"
do_compile() {
    LE_EXTRA_CFLAGS="-I${STAGING_DIR_HOST}/usr/include -I${STAGING_DIR_HOST}/usr/include/linux"
    cd ${KERNEL_PLATFORM_PATH}
    LE_EXTRA_CFLAGS="${LE_EXTRA_CFLAGS}" \
    BUILD_CONFIG=${KERNEL_BUILD_CONFIG} \
    EXT_MODULES=${EXT_MODULES} \
    KERNEL_KIT=${KERNEL_PREBUILT_PATH} \
    OUT_DIR=${KERNEL_OUT_PATH}/ \
    INPLACE_COMPILE=y \
    MODULE_OUT=${S} \
    VIDEO_ROOT=${WORKDIR}/vendor/qcom/opensource/video-driver \
    TARGET_BOARD_PLATFORM=${BASEMACHINE}-le \
    ENABLE_DDK_BUILD=true \
    ./build/build_module.sh \
    KBUILD_EXTRA_SYMBOLS=${STAGING_DIR_HOST}/lib/modules/${KERNEL_VERSION}/Module.symvers
}

do_install() {
    ## copy *all* the generated KOs to ${D}$/lib/modules/${KERNEL_VERSION}
    ## please notice all the output layout maybe different, please modify as per the actual situation
    install -d ${D}${nonarch_base_libdir}/modules/${KERNEL_VERSION}
    install -m 0644 ${WORKDIR}/${BASEMACHINE}/video_load.conf -D ${D}${sysconfdir}/modules-load.d/video_load.conf
    install -m 0644 ${B}/msm_video.ko -D ${D}${base_libdir}/modules/${KERNEL_VERSION}/msm_video.ko
    ## copy Module.symvers so that other DLKM can make use of symbol of current DLKM
    install -m 0644 ${B}/Module.symvers -D ${D}/${base_libdir}/modules/${KERNEL_VERSION}/video-kernel/Module.symvers
    install -m 0644  ${B}/include/uapi/vidc/media/v4l2_vidc_extensions.h -D ${D}/usr/include/vidc/media/v4l2_vidc_extensions.h
}
do_deploy() {
# Deploy unstripped kernel modules into ${DEPLOYDIR}/kernel_modules for debugging purposes
    install -d ${DEPLOYDIR}/kernel_modules
    for kmod in $(find ${D} -name "*.ko") ; do
        install -m 0644 $kmod ${DEPLOYDIR}/kernel_modules
    done
}

addtask deploy after do_install before do_package

FILES:${PN} += "${base_libdir}/modules/${KERNEL_VERSION}/*"
FILES:${PN} += "${nonarch_base_libdir}/modules/${KERNEL_VERSION}/*"
