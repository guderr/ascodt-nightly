cmake_minimum_required(VERSION 2.8.6)
PROJECT(Athlet)

find_package(JNI)

if (JNI_FOUND)
    message (STATUS "JNI_INCLUDE_DIRS=${JNI_INCLUDE_DIRS}")
    message (STATUS "JNI_LIBRARIES=${JNI_LIBRARIES}")
    INCLUDE_DIRECTORIES(${JNI_INCLUDE_DIRS})
endif()

include(CMakeForceCompiler)	   
IF(${CMAKE_SYSTEM_NAME} MATCHES "Linux")
CMAKE_FORCE_CXX_COMPILER(icpc "Intel Cxx Compiler") 
CMAKE_FORCE_C_COMPILER(icc "Intel C Compiler")     
CMAKE_FORCE_Fortran_COMPILER(ifort "Intel Fortran Compiler")
ELSE()
CMAKE_FORCE_CXX_COMPILER(icl.exe "Intel Cxx Compiler") 
CMAKE_FORCE_C_COMPILER(icl.exe "Intel C Compiler")     
CMAKE_FORCE_Fortran_COMPILER(ifort.exe "Intel Fortran Compiler")
ENDIF(${CMAKE_SYSTEM_NAME} MATCHES "Linux")
message(STATUS ${CMAKE_Fortran_COMPILER})
message(STATUS ${CMAKE_CXX_COMPILER})
message(STATUS ${CMAKE_C_COMPILER})
enable_language( Fortran ) 

option(EXTERNAL_ATHLET "Use external ATHLET" ON)
if(EXTERNAL_ATHLET)
#	include athlet header files *.mod
	find_path(ATHLET_PATH "modlib")
	INCLUDE_DIRECTORIES(${ATHLET_PATH})
	set(ATHLET_LIBDIR "/work_fast/atanasoa/Programme/workspace_new/Athlet/LINUX/LIBRARIES")
	find_library(ATHLET_LIB NAMES athlet PATHS ${ATHLET_LIBDIR})

else(EXTERNAL_ATHLET)
	message(FATAL_ERROR "For this version of QC you need to enable external ATHLET")
endif(EXTERNAL_ATHLET)

INCLUDE_DIRECTORIES(../../commons)
INCLUDE_DIRECTORIES(../../src)
INCLUDE_DIRECTORIES(../../components/java)
SET(EXECUTABLE_OUTPUT_PATH "../../native")
SET(LIBRARY_OUTPUT_PATH  "../../native/lib")



SET(SRC_COMP_EXEC
	../../components/java/grs/AthletAbstractImplementation.f90
	../../src/grs/AthletImplementation.f90 
	../../src/grs/athlet.f
	../../src/grs/n3inp.f90
	../../src/grs/n3inte.f90
	../../src/grs/n3rest.f90
	../../src/grs/n3set.f90
	../../components/java/grs/AthletFProxy.f90
	../../components/java/grs/AthletC2FProxy.cpp 
	
)

SET(SRC_COMP_LIB
	../../components/java/grs/AthletAbstractImplementation.f90
	../../src/grs/athlet.f
	../../src/grs/AthletImplementation.f90)

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
set(SRC_COMP_EXEC
   	${SRC_COMP_EXEC}
   	../../components/java/grs/n3inpNative2JavaPlainPort.cpp 
   	../../components/java/grs/n3inpNative2NativePlainPort.cpp
	../../components/java/grs/n3inpCxx2SocketPlainPort.cpp
	../../components/java/grs/n3inpC2CxxSocketPlainPort.cpp 
	../../components/java/grs/n3inpNativeDispatcher.cpp
	../../components/java/grs/n3inpNativeSocketDispatcher.cpp
	../../components/java/grs/n3inpCProxyNativeSocketDispatcher.cpp
	../../components/java/grs/n3inp2SocketPlainPort.f90
	../../components/java/grs/n3inp2SocketPlainPortProxy.f90
	../../components/java/grs/n3inpFNativeSocketDispatcher.f90
	../../components/java/grs/n3inpFProxyNativeSocketDispatcher.f90
)

set(SRC_COMP_LIB
   	${SRC_COMP_LIB}
   	../../components/java/grs/n3inpNative2JavaPlainPort.cpp 
   	../../components/java/grs/n3inpNative2NativePlainPort.cpp
	../../components/java/grs/n3inpCxx2SocketPlainPort.cpp
	../../components/java/grs/n3inpC2CxxSocketPlainPort.cpp 
	../../components/java/grs/n3inpNativeDispatcher.cpp
	../../components/java/grs/n3inpNativeSocketDispatcher.cpp
	../../components/java/grs/n3inpCProxyNativeSocketDispatcher.cpp
	../../components/java/grs/n3inp2SocketPlainPort.f90
	../../components/java/grs/n3inp2SocketPlainPortProxy.f90
	../../components/java/grs/n3inpFNativeSocketDispatcher.f90
	../../components/java/grs/n3inpFProxyNativeSocketDispatcher.f90
)
set(SRC_COMP_EXEC
   	${SRC_COMP_EXEC}
   	../../components/java/grs/n3setNative2JavaPlainPort.cpp 
   	../../components/java/grs/n3setNative2NativePlainPort.cpp
	../../components/java/grs/n3setCxx2SocketPlainPort.cpp
	../../components/java/grs/n3setC2CxxSocketPlainPort.cpp 
	../../components/java/grs/n3setNativeDispatcher.cpp
	../../components/java/grs/n3setNativeSocketDispatcher.cpp
	../../components/java/grs/n3setCProxyNativeSocketDispatcher.cpp
	../../components/java/grs/n3set2SocketPlainPort.f90
	../../components/java/grs/n3set2SocketPlainPortProxy.f90
	../../components/java/grs/n3setFNativeSocketDispatcher.f90
	../../components/java/grs/n3setFProxyNativeSocketDispatcher.f90
)

set(SRC_COMP_LIB
   	${SRC_COMP_LIB}
   	../../components/java/grs/n3setNative2JavaPlainPort.cpp 
   	../../components/java/grs/n3setNative2NativePlainPort.cpp
	../../components/java/grs/n3setCxx2SocketPlainPort.cpp
	../../components/java/grs/n3setC2CxxSocketPlainPort.cpp 
	../../components/java/grs/n3setNativeDispatcher.cpp
	../../components/java/grs/n3setNativeSocketDispatcher.cpp
	../../components/java/grs/n3setCProxyNativeSocketDispatcher.cpp
	../../components/java/grs/n3set2SocketPlainPort.f90
	../../components/java/grs/n3set2SocketPlainPortProxy.f90
	../../components/java/grs/n3setFNativeSocketDispatcher.f90
	../../components/java/grs/n3setFProxyNativeSocketDispatcher.f90
)
set(SRC_COMP_EXEC
   	${SRC_COMP_EXEC}
   	../../components/java/grs/n3restNative2JavaPlainPort.cpp 
   	../../components/java/grs/n3restNative2NativePlainPort.cpp
	../../components/java/grs/n3restCxx2SocketPlainPort.cpp
	../../components/java/grs/n3restC2CxxSocketPlainPort.cpp 
	../../components/java/grs/n3restNativeDispatcher.cpp
	../../components/java/grs/n3restNativeSocketDispatcher.cpp
	../../components/java/grs/n3restCProxyNativeSocketDispatcher.cpp
	../../components/java/grs/n3rest2SocketPlainPort.f90
	../../components/java/grs/n3rest2SocketPlainPortProxy.f90
	../../components/java/grs/n3restFNativeSocketDispatcher.f90
	../../components/java/grs/n3restFProxyNativeSocketDispatcher.f90
)

set(SRC_COMP_LIB
   	${SRC_COMP_LIB}
   	../../components/java/grs/n3restNative2JavaPlainPort.cpp 
   	../../components/java/grs/n3restNative2NativePlainPort.cpp
	../../components/java/grs/n3restCxx2SocketPlainPort.cpp
	../../components/java/grs/n3restC2CxxSocketPlainPort.cpp 
	../../components/java/grs/n3restNativeDispatcher.cpp
	../../components/java/grs/n3restNativeSocketDispatcher.cpp
	../../components/java/grs/n3restCProxyNativeSocketDispatcher.cpp
	../../components/java/grs/n3rest2SocketPlainPort.f90
	../../components/java/grs/n3rest2SocketPlainPortProxy.f90
	../../components/java/grs/n3restFNativeSocketDispatcher.f90
	../../components/java/grs/n3restFProxyNativeSocketDispatcher.f90
)
set(SRC_COMP_EXEC
   	${SRC_COMP_EXEC}
   	../../components/java/grs/n3inteNative2JavaPlainPort.cpp 
   	../../components/java/grs/n3inteNative2NativePlainPort.cpp
	../../components/java/grs/n3inteCxx2SocketPlainPort.cpp
	../../components/java/grs/n3inteC2CxxSocketPlainPort.cpp 
	../../components/java/grs/n3inteNativeDispatcher.cpp
	../../components/java/grs/n3inteNativeSocketDispatcher.cpp
	../../components/java/grs/n3inteCProxyNativeSocketDispatcher.cpp
	../../components/java/grs/n3inte2SocketPlainPort.f90
	../../components/java/grs/n3inte2SocketPlainPortProxy.f90
	../../components/java/grs/n3inteFNativeSocketDispatcher.f90
	../../components/java/grs/n3inteFProxyNativeSocketDispatcher.f90
)

set(SRC_COMP_LIB
   	${SRC_COMP_LIB}
   	../../components/java/grs/n3inteNative2JavaPlainPort.cpp 
   	../../components/java/grs/n3inteNative2NativePlainPort.cpp
	../../components/java/grs/n3inteCxx2SocketPlainPort.cpp
	../../components/java/grs/n3inteC2CxxSocketPlainPort.cpp 
	../../components/java/grs/n3inteNativeDispatcher.cpp
	../../components/java/grs/n3inteNativeSocketDispatcher.cpp
	../../components/java/grs/n3inteCProxyNativeSocketDispatcher.cpp
	../../components/java/grs/n3inte2SocketPlainPort.f90
	../../components/java/grs/n3inte2SocketPlainPortProxy.f90
	../../components/java/grs/n3inteFNativeSocketDispatcher.f90
	../../components/java/grs/n3inteFProxyNativeSocketDispatcher.f90
)
 	 
ADD_EXECUTABLE(Athlet ${SRC_COMP_EXEC} )
target_link_libraries(Athlet ${ATHLET_LIB})
ADD_LIBRARY(Athlet-dev STATIC ${SRC_COMP_LIB} )
SET_TARGET_PROPERTIES(Athlet PROPERTIES LINKER_LANGUAGE Fortran)

