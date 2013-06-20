!
! ASCoDT - Advanced Scientific Computing Development Toolkit
!
! This file was generated by ASCoDT's simplified SIDL compiler.
!
! Authors: Tobias Weinzierl, Atanas Atanasov   
!
module grs_AthletImplementation
use grs_athletAbstractImplementation
implicit none

type, extends ( AthletAbstractImplementation ), public :: AthletImplementation 
     ! Declaration of private members
     private 
     !list of procedures
     contains
     procedure,public::createInstance
     procedure,public::destroyInstance
     
end type AthletImplementation
type(AthletImplementation ),allocatable::Athlet_instance
contains 

subroutine createInstance(this)
    class( AthletImplementation)::this
    !put your initialization code here
end subroutine createInstance

subroutine destroyInstance(this)
     class( AthletImplementation)::this
     !put your destructor code here
end subroutine destroyInstance



end module  grs_AthletImplementation



PROGRAM Athlet
    call socket_client_loop()

end PROGRAM