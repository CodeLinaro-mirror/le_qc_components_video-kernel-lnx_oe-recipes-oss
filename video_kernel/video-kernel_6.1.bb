inherit module deploy

DESCRIPTION = "QTI Video driver"
LICENSE = "GPL-2.0-with-Linux-syscall-note"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/GPL-2.0-only;md5=801f80980d171dd6425610833a22dbe6"

DEFAULT_PREFERENCE = "-1"

DEPENDS += "virtual/kernel bc-native rsync-native kernel-module-mmrm-kernel kernel-module-synx-kernel synx-kernel-header"

do_configure[depends] += "virtual/kernel:do_shared_workdir"

FILESPATH =. "${WORKSPACE}:"
SRC_URI  =  "file://vendor/qcom/opensource/video-driver/"
SRC_URI +=  "file://${BASEMACHINE}/video_load.conf"

S = "${WORKDIR}/vendor/qcom/opensource/video-driver"

RPROVIDES:${PN} += "kernel-module-msm-video-${KERNEL_VERSION}"

DEPENDS += "virtual/kernel-toolchain-native"
EXTRA_OEMAKE += "M=${S}"
EXTRA_OEMAKE += "EXTRA_CFLAGS+=-I${STAGING_INCDIR}"
KERNEL_MODULES = "msm_video"
MAKE_TARGETS = "modules"

KERNEL_CC = "${STAGING_BINDIR_NATIVE}/clang/bin/clang -target ${TARGET_ARCH}${TARGET_VENDOR}-${TARGET_OS}"

#do_compile() {
#   make CC="${KERNEL_CC}" -C ${STAGING_KERNEL_BUILDDIR} M=${S} modules
#}

do_install() {
    install -m 0644 ${WORKDIR}/${BASEMACHINE}/video_load.conf -D ${D}${sysconfdir}/modules-load.d/video_load.conf
    install -m 0644 ${WORKDIR}/vendor/qcom/opensource/video-driver/msm_video.ko -D ${D}${base_libdir}/modules/${KERNEL_VERSION}/msm_video.ko
    install -m 0644 ${S}/include/uapi/vidc/media/v4l2_vidc_extensions.h -D ${D}/usr/include/vidc/media/v4l2_vidc_extensions.h
}

FILES:${PN} = "${sysconfdir}/*"
FILES:${PN} += "${base_libdir}/*"
