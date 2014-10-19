/*
	This file is part of ALIZE which is an open-source tool for 
	speaker recognition.

    ALIZE is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as 
    published by the Free Software Foundation, either version 3 of 
    the License, or any later version.

    ALIZE is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public 
    License along with ALIZE.
    If not, see <http://www.gnu.org/licenses/>.
        
	ALIZE is a development project initiated by the ELISA consortium
	[alize.univ-avignon.fr/] and funded by the French Research 
	Ministry in the framework of the TECHNOLANGUE program 
	[www.technolangue.net]

	The ALIZE project team wants to highlight the limits of voice
	authentication in a forensic context.
	The "Person  Authentification by Voice: A Need of Caution" paper 
	proposes a good overview of this point (cf. "Person  
	Authentification by Voice: A Need of Caution", Bonastre J.F., 
	Bimbot F., Boe L.J., Campbell J.P., Douglas D.A., Magrin-
	chagnolleau I., Eurospeech 2003, Genova].
	The conclusion of the paper of the paper is proposed bellow:
	[Currently, it is not possible to completely determine whether the 
	similarity between two recordings is due to the speaker or to other 
	factors, especially when: (a) the speaker does not cooperate, (b) there 
	is no control over recording equipment, (c) recording conditions are not 
	known, (d) one does not know whether the voice was disguised and, to a 
	lesser extent, (e) the linguistic content of the message is not 
	controlled. Caution and judgment must be exercised when applying speaker 
	recognition techniques, whether human or automatic, to account for these 
	uncontrolled factors. Under more constrained or calibrated situations, 
	or as an aid for investigative purposes, judicious application of these 
	techniques may be suitable, provided they are not considered as infallible.
	At the present time, there is no scientific process that enables one to 
	uniquely characterize a person=92s voice or to identify with absolute 
	certainty an individual from his or her voice.]
	Contact Jean-Francois Bonastre for more information about the licence or
	the use of ALIZE

	Copyright (C) 2003-2010
	Laboratoire d'informatique d'Avignon [lia.univ-avignon.fr]
	ALIZE admin [alize@univ-avignon.fr]
	Jean-Francois Bonastre [jean-francois.bonastre@univ-avignon.fr]
*/

#if !defined(ALIZE_DistribGD_cpp)
#define ALIZE_DistribGD_cpp


#if defined(_WIN32)
  #include <cfloat> // for _isnan()
  #define ISNAN(x) _isnan(x)
//#elif defined(__APPLE__)
//  #define ISNAN(x) std::isnan(x)
#elif defined(linux) || defined(__linux) || defined(__CYGWIN__) || defined(__APPLE__) 
  #define ISNAN(x) isnan(x)
#else
  #error "Unsupported OS\n"
#endif

#include <new>
#include <cmath>
#include <cstdlib>
#include <memory.h>
#include "DistribGD.h"
#include "alizeString.h"
#include "Feature.h"
#include "Exception.h"
#include "Config.h"

#include <stdio.h>
#include <iostream>
using namespace alize;
using namespace std;

// long tmp1[]= {1.80429e+09, 1.68169e+09, 1.95775e+09, 7.19885e+08, 5.96517e+08, 1.0252e+09,  7.83369e+08, 2.0449e+09,  1.36518e+09, 3.04089e+08, 3.50052e+07, 
// 2.94703e+08, 3.36466e+08, 2.78723e+08, 2.14517e+09, 1.10151e+09, 1.31563e+09, 1.36913e+09, 1.05996e+09, 6.28175e+08, 1.13118e+09, 8.59484e+08, 
// 6.08414e+08, 1.73458e+09, 1.49798e+08, 1.12957e+09, 4.12776e+08, 1.91176e+09, 1.37807e+08, 9.82907e+08, 5.11702e+08, 1.93748e+09, 5.7266e+08,
//   8.05751e+08, 1.10066e+09, 1.14162e+09, 9.3982e+08,  1.9989e+09,  6.10515e+08, 1.37434e+09, 1.47717e+09, 9.45117e+08, 1.7807e+09,  
// 4.91705e+08, 7.52393e+08, 2.054e+09,   1.41155e+09, 9.43948e+08, 8.55636e+08, 1.46935e+09, 1.03614e+09, 2.04065e+09, 3.17097e+08, 
// 1.37671e+09, 1.33057e+09, 1.68793e+09, 9.59997e+08, 4.02724e+08, 1.19495e+09, 3.64228e+08, 2.21558e+08, 1.06396e+09, 2.11474e+09,
//  1.46983e+09, 1.61012e+09, 6.31705e+08, 1.25518e+09, 3.27255e+08, 2.69455e+08, 3.52406e+08, 1.60052e+08, 1.12806e+08, 3.7841e+08, 
// 	      1.71326e+09, 1.40996e+09, 1.37323e+09, 2.00748e+08, 1.11714e+09, 1.50123e+08, 9.90893e+08, 1.23119e+09};

//   long tmp2[]={8.46931e+08, 1.71464e+09, 4.24238e+08, 1.64976e+09, 1.18964e+09, 1.35049e+09, 1.10252e+09, 1.96751e+09, 1.54038e+09, 
// 1.30346e+09, 5.21595e+08, 1.72696e+09, 8.61022e+08, 2.33665e+08, 4.68703e+08, 1.80198e+09, 6.35723e+08, 1.1259e+09, 
// 2.08902e+09, 1.65648e+09, 1.65338e+09, 1.91454e+09, 7.56899e+08, 1.97359e+09, 2.03866e+09, 1.84804e+08, 1.42427e+09, 
// 7.49242e+08, 4.29992e+07, 1.35497e+08, 2.08442e+09, 1.82734e+09, 1.15913e+09, 1.63262e+09, 1.43393e+09, 8.43539e+07, 
// 2.0011e+09, 1.54823e+09, 1.58599e+09, 7.60314e+08, 3.56427e+08, 1.88995e+09, 7.09394e+08, 1.9185e+09, 
// 1.47461e+09, 1.2641e+09, 1.84399e+09, 1.98421e+09, 1.7497e+09, 1.9563e+09, 4.63481e+08, 1.97596e+09, 1.89207e+09, 
// 9.27613e+08, 6.0357e+08, 6.60261e+08, 4.8556e+08, 5.93209e+08, 8.9443e+08, 1.94735e+09, 2.70745e+08, 
// 1.63311e+09, 2.00791e+09, 8.22891e+08, 7.91699e+08, 4.98778e+08, 5.24872e+08, 1.57228e+09, 1.70396e+09, 1.60003e+09, 
// 2.04033e+09, 1.12005e+09, 5.1553e+08, 1.57336e+09, 2.07749e+09, 1.63152e+09, 2.89701e+08, 1.68002e+08, 4.39493e+08, 
// 	       1.76024e+09, 1.6226e+09};
// int tmpidx=0;
// int tmpidxmax=sizeof(tmp2)/sizeof(tmp2[0]);
//-------------------------------------------------------------------------
DistribGD::DistribGD(unsigned long vectSize)
 :Distrib(vectSize), _covInvVect(_vectSize, _vectSize)
{ reset(); }
//-------------------------------------------------------------------------
DistribGD::DistribGD(const Config& c)
 :Distrib(c.getParam_vectSize()>0?c.getParam_vectSize():1),
 _covInvVect(_vectSize, _vectSize) { reset(); }
//-------------------------------------------------------------------------

#ifdef __ANDROID_API__
#include <jni.h>
#include <android/log.h>
#else
#include <stdio.h>
#endif

void DistribGD::reset() // random init
{
  //srand(time(NULL));
  _covVect.setSize(_vectSize);
  //#define RAND_MAX00 2147483647.0
  for (unsigned long i=0; i< _vectSize; i++)
  {
    //_covVect[i] = (0.08+1.0)/(RAND_MAX+1.0); // always > 0.0
    //_meanVect[i] = (double)0.72*2/RAND_MAX - 1.0;
    // double t1 = tmp1[tmpidx], t2=tmp2[tmpidx];
//     if (++tmpidx == tmpidxmax)
//       tmpidx = 0;
//     _covVect[i] = (t1+1.0)/(RAND_MAX+1.0); // always > 0.0
//     _meanVect[i] = (double)t2*2/RAND_MAX - 1.0;
    // cout <<"DistribGD saving tmps tmp1=" << tmp1 << ",    tmp2="<< tmp2 <<endl;
    _covVect[i] = (rand()+1.0)/(RAND_MAX+1.0); // always > 0.0
    _meanVect[i] = (double)rand()*2/RAND_MAX - 1.0;
  }
#ifdef __ANDROID_API__
  //  __android_log_print(ANDROID_LOG_DEBUG, "DistribGD", "-computeall reset \n");
#else
  //printf("DistribGD -computeall reset \n");
#endif

  computeAll();
}
//-------------------------------------------------------------------------
DistribGD& DistribGD::create(const K&, unsigned long vectSize)
{
  DistribGD* p = new (std::nothrow) DistribGD(vectSize);
  assertMemoryIsAllocated(p, __FILE__, __LINE__);
  return *p;
}
//-------------------------------------------------------------------------
DistribGD& DistribGD::create(const K&, const Config& c)
{ return create(K::k, c.getParam_vectSize()); }
//-------------------------------------------------------------------------
DistribGD::DistribGD(const DistribGD& d)
:Distrib(d._vectSize), _covVect(d._covVect), _covInvVect(d._covInvVect)
{
  _meanVect = d._meanVect;
  _det = d._det;
  _cst = d._cst;
}
//-------------------------------------------------------------------------
const Distrib& DistribGD::operator=(const Distrib& d) // virtual
{
  const DistribGD* p = dynamic_cast<const DistribGD*>(&d);
  if (p == NULL)
    throw Exception("incompatible distrib", __FILE__, __LINE__);
  return operator=(*p);
}
//-------------------------------------------------------------------------
const DistribGD& DistribGD::operator=(const DistribGD& d)
{
  if (_vectSize != d.getVectSize())
    throw Exception("target distrib vectSize ("
        + String::valueOf(_vectSize) + ") != source distrib vectSize ("
        + String::valueOf(d._vectSize) + ")", __FILE__, __LINE__);
  _meanVect = d._meanVect;
  _covInvVect = d._covInvVect;
  _covVect = d._covVect;
  _det = d._det;
  _cst = d._cst;
  return *this;
}
//-------------------------------------------------------------------------
bool DistribGD::operator==(const Distrib& d) const
{
  const DistribGD* p = dynamic_cast<const DistribGD*>(&d);
  return (p != NULL && _meanVect == p->_meanVect &&
      _covInvVect == p->_covInvVect);
}  
//-------------------------------------------------------------------------
DistribGD& DistribGD::duplicate(const K&) const
{ return static_cast<DistribGD&>(clone()); }
//-------------------------------------------------------------------------
Distrib& DistribGD::clone() const // private
{
  DistribGD* p = new (std::nothrow) DistribGD(*this);
  assertMemoryIsAllocated(p, __FILE__, __LINE__);
  return *p;
}

//-------------------------------------------------------------------------
// TODO : A OPTIMISER !!
//-------------------------------------------------------------------------
lk_t DistribGD::computeLK(const Feature& frame) const
{
  if (frame.getVectSize() != _vectSize)
    throw Exception("distrib vectSize ("
        + String::valueOf(_vectSize) + ") != feature vectSize ("
      + String::valueOf(frame.getVectSize()) + ")", __FILE__, __LINE__);
  real_t tmp = 0.0;
  real_t*      m = _meanVect.getArray();
  real_t*      c = _covInvVect.getArray();
  Feature::data_t* f = frame.getDataVector();

  for (unsigned long i=0; i<_vectSize; i++)
    tmp += (f[i] - m[i]) * (f[i] - m[i]) * c[i];

  tmp = _cst * exp(-0.5*tmp);
  if (ISNAN(tmp)){
#ifdef __ANDROID_API__
    //    __android_log_print(ANDROID_LOG_DEBUG, "DistribGD", "-computLK1 ****isnan found**** %f\n", tmp);
#else
    //printf("DistribGD -computLK1 ****isnan found**** %f\n", tmp);
#endif
  
    return EPS_LK;
  }
  return tmp;
}
//-------------------------------------------------------------------------
lk_t DistribGD::computeLK(const Feature& frame, unsigned long i) const
{
  real_t fm = frame[i] - _meanVect[i];
  real_t tmp = _cst * exp(-0.5 * fm * fm * _covInvVect[i]);
  if (ISNAN(tmp)) {
#ifdef __ANDROID_API__
    //    __android_log_print(ANDROID_LOG_DEBUG, "DistribGD", "-computLK2 ****isnan found**** %f\n", tmp);
#else
    // printf("DistribGD -computLK2 ****isnan found**** %f\n", tmp);
#endif
    return EPS_LK;
 }
  return tmp;
}
//-------------------------------------------------------------------------


void DistribGD::computeAll()
{
  real_t* vect = getCovVect().getArray();
  assert(vect != NULL);
  unsigned long i;

   // compute det --------------------------------

  _det = 1.0;

#ifdef __ANDROID_API__
  //  __android_log_print(ANDROID_LOG_DEBUG, "DistribGD", "-computeall ********** det=%f; vect[0]=%f\n",_det, vect[0]);
#else
  //   printf("DistribGD -computeall ********** det=%f; vect[0]=%f\n",_det,vect[0]);
#endif

  for (i=0; i< _vectSize; i++)
  {
     _det *= vect[i];
  }

  // compute covInv ----------------------------

  for (i=0; i< _vectSize; i++)
  {
  	if( vect[i] == 0.0) { throw Exception( "Assertion 'vect[i] != 0.0' - Can't invert covariance vector - This error is mainly due to bad parametric data !", __FILE__, __LINE__) ; }
    _covInvVect[i] = 1.0/vect[i];
  }

  // compute cst -------------------------------


  if (_det > EPS_LK) {
    _cst = 1.0 / ( pow(_det, 0.5) * pow( PI2 , _vectSize/2.0 ) );

#ifdef __ANDROID_API__
    //    __android_log_print(ANDROID_LOG_DEBUG, "DistribGD", "-computeall  **1**** _cst=%f; _det=%f ; powpow=%f\n",_cst,
    //		_det, pow(_det, 0.5) * pow( PI2 , _vectSize/2.0 )  );
    //  __android_log_print(ANDROID_LOG_DEBUG, "DistribGD", "-computeall  **********_cst=%f; _det=%f ; pow_det.5=%f; powPI2_vec=%f; powpow=%f\n",_cst,
    //		_det, pow(_det, 0.5) , pow( PI2 , _vectSize/2.0 ), pow(_det, 0.5) * pow( PI2 , _vectSize/2.0 )  );
#else
    //   printf("DistribGD -computeall  **1*******_cst=%f; _det=%f ; powpow=%f; \n", _cst, _det, pow(_det, 0.5) * pow( PI2 , _vectSize/2.0 )  );
    //
   // printf("DistribGD -computeall  **********_cst=%f; _det=%f ; pow_det.5=%f; powPI2_vec=%f; powpow=%f\n",_cst,
   //  _det, pow(_det, 0.5) , pow( PI2 , _vectSize/2.0 ), pow(_det, 0.5) * pow( PI2 , _vectSize/2.0 )  );
#endif

  }
   else {
    _cst = 1.0 / ( pow(EPS_LK, 0.5) * pow( PI2 , _vectSize/2.0 ) );
#ifdef __ANDROID_API__
    //  __android_log_print(ANDROID_LOG_DEBUG, "DistribGD", "-computeall **2*******_cst=%f; _det=%f ;  powpow=%f\n",_cst,
    //  _det,  pow(EPS_LK, 0.5) * pow( PI2 , _vectSize/2.0 ) );
  //  __android_log_print(ANDROID_LOG_DEBUG, "DistribGD", "-computeall **********_cst=%f; _det=%f ; pow_epslk.5=%f; powPI2_vec=%f; powpow=%f\n",_cst,
  //  _det, pow(EPS_LK, 0.5), pow( PI2 , _vectSize/2.0 ), pow(EPS_LK, 0.5) * pow( PI2 , _vectSize/2.0 ) );
#else
    //   printf("DistribGD -computeall  **2*******_cst=%f; _det=%f ;  powpow=%f\n",_cst,
    //  _det,  pow(EPS_LK, 0.5) * pow( PI2 , _vectSize/2.0 ) );
   //   printf("DistribGD -computeall  **********_cst=%f; _det=%f ; pow_epslk.5=%f; powPI2_vec=%f; powpow=%f\n",_cst,
   //  _det, pow(EPS_LK, 0.5), pow( PI2 , _vectSize/2.0 ), pow(EPS_LK, 0.5) * pow( PI2 , _vectSize/2.0 ) );
#endif
   }


  //
  _covVect.setSize(0, true); // set capacity to 0 too
}
//-------------------------------------------------------------------------
void DistribGD::setCov(real_t v, unsigned long i)
{
  if (v < MIN_COV)
    getCovVect()[i] = MIN_COV;
  else
    getCovVect()[i] = v;
}
//-------------------------------------------------------------------------
void DistribGD::setCovInv(const K&, real_t v, unsigned long i)
{ _covInvVect[i] = v; }
//-------------------------------------------------------------------------
real_t DistribGD::getCov(unsigned long i)
{ return getCovVect()[i];}
//-------------------------------------------------------------------------
real_t DistribGD::getCov(unsigned long i) const 
{ return getCovVect()[i];}
//-------------------------------------------------------------------------
real_t DistribGD::getCovInv(unsigned long i) const {return _covInvVect[i];}
//-------------------------------------------------------------------------
DoubleVector& DistribGD::getCovInvVect() { return _covInvVect; }
//-------------------------------------------------------------------------
const DoubleVector& DistribGD::getCovInvVect() const { return _covInvVect; }
//-------------------------------------------------------------------------
DoubleVector& DistribGD::getCovVect()
{
  return const_cast<DoubleVector&>(
          const_cast<const DistribGD*>(this)->getCovVect());
}
//-------------------------------------------------------------------------
const DoubleVector& DistribGD::getCovVect() const
{
  if (_covVect.size() != _vectSize)
  {
    _covVect.setSize(_vectSize);
    for (unsigned long i=0; i< _vectSize; i++)
    {
      if (_covInvVect[i] < 1.0/MIN_COV)
        _covVect[i] = 1.0/_covInvVect[i];
      else
        _covVect[i] = MIN_COV;
    }
  }
  return _covVect;
}

//-------------------------------------------------------------------------
String DistribGD::getClassName() const { return "DistribGD"; }
//-------------------------------------------------------------------------
String DistribGD::toString() const
{
  String s = Object::toString()
  + "\n  vectSize  = " + String::valueOf(_vectSize)
  + "\n  det     = " + String::valueOf(_det)
  + "\n  cst     = " + String::valueOf(_cst);
  
  
  if (_covVect.size() != 0)
    for (unsigned long i=0; i<_vectSize; i++)
    {
      s += "\n  cov[" + String::valueOf(i) + "] = "
        + String::valueOf(_covVect[i])
        + "  covInv[" + String::valueOf(i) + "] = "
        + String::valueOf(_covInvVect[i])
        + "  mean[" + String::valueOf(i) + "] = "
        + String::valueOf(_meanVect[i]);
    }
  else
    for (unsigned long i=0; i<_vectSize; i++)
    {
      s += "\n  covInv[" + String::valueOf(i) + "] = "
        + String::valueOf(_covInvVect[i])
        + "  mean[" + String::valueOf(i) + "] = "
        + String::valueOf(_meanVect[i]);
    }
  return s;
}
//-------------------------------------------------------------------------
DistribGD::~DistribGD() {}
//-------------------------------------------------------------------------
#endif // !defined(ALIZE_DistribGD_cpp)

