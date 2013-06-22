cmake_minimum_required(VERSION 2.8.6)
PROJECT(Quabox)


find_package(JNI)

if (JNI_FOUND)
    message (STATUS "JNI_INCLUDE_DIRS=${JNI_INCLUDE_DIRS}")
    message (STATUS "JNI_LIBRARIES=${JNI_LIBRARIES}")
    INCLUDE_DIRECTORIES(${JNI_INCLUDE_DIRS})
endif()

option(EXTERNAL_ATHLET "Use external ATHLET" ON)
if(EXTERNAL_ATHLET)
#	include athlet header files *.mod
	set(ATHLET_PATH "")
	INCLUDE_DIRECTORIES(/work_fast/atanasoa/Programme/workspace_new/Athlet/LINUX/lin64/modlib64)
	set(ATHLET_LIBDIR "/work_fast/atanasoa/Programme/workspace_new/Athlet/LINUX/LIBRARIES")
	find_library(ATHLET_LIB NAMES athlet PATHS ${ATHLET_LIBDIR})

else(EXTERNAL_ATHLET)
	message(FATAL_ERROR "For this version of QC you need to enable external ATHLET")
endif(EXTERNAL_ATHLET)

include(CMakeForceCompiler)	   
CMAKE_FORCE_CXX_COMPILER(/home/software/intel/bin/icpc "Intel Cxx Compiler") 
CMAKE_FORCE_C_COMPILER(/home/software/intel/bin/icc "Intel C Compiler")     
CMAKE_FORCE_Fortran_COMPILER(/home/software/intel/bin/ifort "Intel Fortran Compiler")
message(STATUS ${CMAKE_Fortran_COMPILER})
message(STATUS ${CMAKE_CXX_COMPILER})
message(STATUS ${CMAKE_C_COMPILER})
enable_language( Fortran ) 

INCLUDE_DIRECTORIES(../../commons)
INCLUDE_DIRECTORIES(../../src)
INCLUDE_DIRECTORIES(../../components/java)
SET(EXECUTABLE_OUTPUT_PATH "../../native")
SET(LIBRARY_OUTPUT_PATH  "../../native/lib")



SET(SRC_COMP_EXEC
	../../components/java/grs/QuaboxAbstractImplementation.f90
	../../src/grs/QuaboxImplementation.f90 
	../../components/java/grs/QuaboxFProxy.f90
	../../components/java/grs/QuaboxC2FProxy.cpp 
	
)

SET(SRC_COMP_LIB
	../../components/java/grs/QuaboxAbstractImplementation.f90
	../../src/grs/QuaboxImplementation.f90)

set(SRC_COMP_EXEC
   	${SRC_COMP_EXEC}
   	../../components/java/de/tum/logging/LogNative2JavaPlainPort.cpp 
   	../../components/java/de/tum/logging/LogNative2NativePlainPort.cpp
	../../components/java/de/tum/logging/LogCxx2SocketPlainPort.cpp
	../../components/java/de/tum/logging/LogC2CxxSocketPlainPort.cpp 
	../../components/java/de/tum/logging/LogNativeDispatcher.cpp
	../../components/java/de/tum/logging/LogNativeSocketDispatcher.cpp
	../../components/java/de/tum/logging/LogCProxyNativeSocketDispatcher.cpp
	../../components/java/de/tum/logging/Log2SocketPlainPort.f90
	../../components/java/de/tum/logging/Log2SocketPlainPortProxy.f90
	../../components/java/de/tum/logging/LogFNativeSocketDispatcher.f90
	../../components/java/de/tum/logging/LogFProxyNativeSocketDispatcher.f90
)

set(SRC_COMP_LIB
   	${SRC_COMP_LIB}
   	../../components/java/de/tum/logging/LogNative2JavaPlainPort.cpp 
   	../../components/java/de/tum/logging/LogNative2NativePlainPort.cpp
	../../components/java/de/tum/logging/LogCxx2SocketPlainPort.cpp
	../../components/java/de/tum/logging/LogC2CxxSocketPlainPort.cpp 
	../../components/java/de/tum/logging/LogNativeDispatcher.cpp
	../../components/java/de/tum/logging/LogNativeSocketDispatcher.cpp
	../../components/java/de/tum/logging/LogCProxyNativeSocketDispatcher.cpp
	../../components/java/de/tum/logging/Log2SocketPlainPort.f90
	../../components/java/de/tum/logging/Log2SocketPlainPortProxy.f90
	../../components/java/de/tum/logging/LogFNativeSocketDispatcher.f90
	../../components/java/de/tum/logging/LogFProxyNativeSocketDispatcher.f90
)
 	 
ADD_EXECUTABLE(Quabox ${SRC_COMP_EXEC} )

target_link_libraries(Quabox ${ATHLET_LIB})
ADD_LIBRARY(Quabox-dev STATIC ${SRC_COMP_LIB} )
SET_TARGET_PROPERTIES(Quabox PROPERTIES LINKER_LANGUAGE Fortran)
