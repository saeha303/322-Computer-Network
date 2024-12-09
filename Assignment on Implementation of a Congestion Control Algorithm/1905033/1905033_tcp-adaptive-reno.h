#ifndef TCP_ADAPTIVE_RENO_H
#define TCP_ADAPTIVE_RENO_H

#include "tcp-westwood-plus.h"

namespace ns3 {

class Packet;
class TcpHeader;
class Time;
class EventId;

/**
 * \ingroup congestionOps
 *
 * \brief An implementation of TCP ADAPTIVE RENO.
 *
 */
class TcpAdaptiveReno : public TcpWestwoodPlus
{
public:
  /**
   * \brief Get the type ID.
   * \return the object TypeId
   */
  static TypeId GetTypeId (void);

  TcpAdaptiveReno (void);
  /**
   * \brief Copy constructor
   * \param sock the object to copy
   */
  TcpAdaptiveReno (const TcpAdaptiveReno& sock);
  ~TcpAdaptiveReno (void);

  /**
   * \brief Filter type (None or Tustin)
   */
  enum FilterType 
  {
    NONE,
    TUSTIN
  };

  uint32_t GetSsThresh (Ptr<const TcpSocketState> tcb,
                                uint32_t bytesInFlight) override;

  void PktsAcked (Ptr<TcpSocketState> tcb, uint32_t packetsAcked,
                          const Time& rtt) override;

  Ptr<TcpCongestionOps> Fork ();

private:

  double EstimateCongestionLevel();

  void EstimateIncWnd(Ptr<TcpSocketState> tcb);

protected:
  virtual void CongestionAvoidance (Ptr<TcpSocketState> tcb, uint32_t segmentsAcked);

  Time                   m_RTT_min;                 //!< Minimum RTT
  Time                   m_RTT_curr;             //!< Current RTT
  Time                   m_RTT_jth_pl;            //!< RTT of j packet loss
  Time                   m_RTT_conj;                //!< Conjestion RTT (j th event)
  Time                   m_RTT_conj_prev;            //!< Previous Conjestion RTT (j-1 th event)

  // Window calculations
  int32_t                m_incWnd;                 //!< Increment Window
  uint32_t               m_baseWnd;                //!< Base Window
  int32_t                m_probeWnd;               //!< Probe Window 
  void EstimateBW (const Time& rtt, Ptr<TcpSocketState> tcb);
};

} // namespace ns3

#endif /* TCP_ADAPTIVE_RENO_H */