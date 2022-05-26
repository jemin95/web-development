package com.cosmo.management.contreller;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.cosmo.management.bean.ChikokuBean;
import com.cosmo.management.bean.CommuteBean;
import com.cosmo.management.bean.CommuteMngBean;
import com.cosmo.management.bean.EmployeeBean;
import com.cosmo.management.bean.GaisyutsuBean;
import com.cosmo.management.bean.HankyuuBean;
import com.cosmo.management.bean.KekkinBean;
import com.cosmo.management.bean.KyuukaBean;
import com.cosmo.management.bean.SoutaiBean;
import com.cosmo.management.bean.StampBean;
import com.cosmo.management.config.LinksConfig;
import com.cosmo.management.enums.KubunEnum;
import com.cosmo.management.service.CommuteService;
 
@Controller
public class CommuteController {

	
	@Autowired
	CommuteService service;

	LinksConfig config = new LinksConfig();
	
	KubunEnum ENUM_KANRISYA = KubunEnum.LEADER_KANRISYA;
	KubunEnum ENUM_GENBARIDA = KubunEnum.LEADER_GENBARIDA;
	KubunEnum ENUM_AM = KubunEnum.JIKANTAI_AM;
	KubunEnum ENUM_PM = KubunEnum.JIKANTAI_PM;
	
	private final String DATE_FORM_YMDHM = "yyyyMMddHHmm";
	private final String DATE_FORM_YMD = "yyyyMMdd";
	private final String DATE_FORM_YM = "yyyyMM";
	private final String DATE_FORM_HM = "HHmm";
	
	private final String REDIRECT_LOGIN = "redirect:/login";
	private final String REDIRECT_COMMUTE = "redirect:/management/commute";
	private final String REDIRECT_COMMUTE_LIST = "redirect:/management/commute/list";
	private final String REDIRECT_CHIKOKU_LIST = "redirect:/management/commute/chikoku/list";
	private final String REDIRECT_GAISYUTSU_LIST = "redirect:/management/commute/gaisyutsu/list";
	private final String REDIRECT_SOUTAI_LIST = "redirect:/management/commute/soutai/list";
	private final String REDIRECT_HANKYUU_LIST = "redirect:/management/commute/hankyuu/list";
	private final String REDIRECT_KYUUKA_LIST = "redirect:/management/commute/kyuuka/list";
	private final String REDIRECT_KEKKIN_LIST = "redirect:/management/commute/kekkin/list";
	
	private final String LOCATION_LOGIN = "/login/login";
	private final String LOCATION_COMMUTE = "/management/Commute/Commute";
	private final String LOCATION_COMMUTE_LIST = "/management/Commute/Commute_List";
	private final String LOCATION_COMMUTE_MNG = "/management/Commute/Commute_mng";
	private final String LOCATION_COMMUTE_UPDATE = "/management/Commute/Commute_Update";
	private final String LOCATION_CHIKOKU = "/management/Commute/Chikoku_Info";
	private final String LOCATION_CHIKOKU_LIST = "/management/Commute/Chikoku_List";
	private final String LOCATION_GAISYUTSU = "/management/Commute/Gaisyutsu_Info";
	private final String LOCATION_GAISYUTSU_LIST = "/management/Commute/Gaisyutsu_List";
	private final String LOCATION_SOUTAI = "/management/Commute/Soutai_Info";
	private final String LOCATION_SOUTAI_LIST = "/management/Commute/Soutai_List";
	private final String LOCATION_HANKYUU = "/management/Commute/Hankyuu_Info";
	private final String LOCATION_HANKYUU_LIST = "/management/Commute/Hankyuu_List";
	private final String LOCATION_KYUUKA = "/management/Commute/Kyuuka_Info";
	private final String LOCATION_KYUUKA_LIST = "/management/Commute/Kyuuka_List";
	private final String LOCATION_KEKKIN = "/management/Commute/Kekkin_Info";
	private final String LOCATION_KEKKIN_LIST = "/management/Commute/Kekkin_List";
	
	private final String UPDATE_MSG = "修整完了しました";
	
	@RequestMapping(value="/management/commute/stamp/{mailAddr}")
	public ResponseEntity<byte[]> getByteStampImage(@PathVariable String mailAddr) {
		
		EmployeeBean employee = new EmployeeBean();
		employee.setMailAddr(mailAddr);
		
		StampBean stampBean = service.getStamp(employee);
		byte[] imageContent = (byte[])stampBean.getFile();
		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.IMAGE_PNG);
		
		return new ResponseEntity<byte[]>(imageContent, headers, HttpStatus.OK);
	}
	
	
//	出退勤
	/**
	 * @param session
	 * @return
	 */
	@RequestMapping(value=LinksConfig.COMMUTE_INFO_LINK, method=RequestMethod.GET)
	public ModelAndView goCommute(HttpSession session) {
		
		ModelAndView model = new ModelAndView();
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			model.setViewName(LOCATION_COMMUTE);
			CommuteBean check = new CommuteBean();
			check.setMailAddr(employee.getMailAddr());
				
			if(null != session.getAttribute("commuteMsg")) {
				model.addObject("msg", session.getAttribute("commuteMsg"));
				session.removeAttribute("commuteMsg");
			}
			if(null != session.getAttribute("chikokuMsg")) {
				model.addObject("cMsg", session.getAttribute("chikokuMsg"));
				session.removeAttribute("chikokuMsg");
			}
			try {
				CommuteBean cmtBean = service.getTodayData(check);
				
				SimpleDateFormat sdF = new SimpleDateFormat(DATE_FORM_YM);
				String date = sdF.format(System.currentTimeMillis());
				check.setDate(date);
				
				List<CommuteBean> list = service.getCommuteList(check);
				
				for(int i=0; i<list.size(); i++) {
					CommuteBean listObject = list.get(i);
					
					if(listObject.getNo() != cmtBean.getNo()) {
						if(null==listObject.getTaikinTime() || listObject.getTaikinTime().equals("")) {
							listObject.setTaikinTime("1800");
							service.insertTaikin(listObject);
							service.insertZangyou(listObject);
						}
					}
				}
				model.addObject("commute", cmtBean); 
				if(900 < Integer.parseInt(cmtBean.getSyukinTime()))
					model.addObject("chikokuMsg", "今日は遅刻です。");
				return model; 
			}
			catch(Exception e) {
				SimpleDateFormat sdForm = new SimpleDateFormat(DATE_FORM_YMD);
				String today = sdForm.format(System.currentTimeMillis());
				check.setDate(today);
				
				model.addObject("commute", check); 
				return model;
			}
		}
		catch(NullPointerException e) {
			model.setViewName(LOCATION_LOGIN);
			return model;
		}
	}
		
	@RequestMapping(value=LinksConfig.COMMUTE_INFO_SYUKIN_LINK, method=RequestMethod.POST)
	public String commuteSyukin(HttpSession session) {
		
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			CommuteBean cmtBean = new CommuteBean();
			cmtBean.setMailAddr(employee.getMailAddr());
			
			if(null == service.getTodayData(cmtBean)) {
				
				SimpleDateFormat sdForm = new SimpleDateFormat(DATE_FORM_YMDHM);
				String time = sdForm.format(System.currentTimeMillis());
				String syukinTime = time.substring(8, 12);
				
				cmtBean.setDate(time.substring(0, 8));
				cmtBean.setSyukinTime(syukinTime);
				cmtBean.setInsertMail(employee.getMailAddr());
				cmtBean.setInsertDateTime(new Date());
				service.insertSyukin(cmtBean);
				
				/**
				   * 遅刻入力
				 */
//				if(900 < Integer.parseInt(syukinTime)) {
//					service.insertChikoku(service.getTodayData(cmtBean));
//					session.setAttribute("chikokuMsg", "今日、遅刻しましたので、遅刻の理由を入力してください。");
//				}
			}
			return REDIRECT_COMMUTE;
		}
		catch(NullPointerException e){
			return REDIRECT_LOGIN;
		}
	}
	
	@RequestMapping(value=LinksConfig.COMMUTE_INFO_TAIKIN_LINK, method=RequestMethod.POST)
	public String commuteTaikin(HttpSession session) {
		
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			
			CommuteBean check = new CommuteBean();
			check.setMailAddr(employee.getMailAddr());
			
			if(null == service.getTodayData(check)) {
				return REDIRECT_COMMUTE;
			}
			else {
				CommuteBean cmtBean = service.getTodayData(check);
				String taikinTime = cmtBean.getTaikinTime();
				if(null == taikinTime || taikinTime.equals("")) {
					
//					GaisyutsuListBean gslBean = service.getGaisyutsuData(cmtBean);
//					if(null != gslBean && null == gslBean.getEndTime()) {
//						session.setAttribute("commuteMsg", "まだ外出の復帰をしなかったです。");
//						return REDIRECT_COMMUTE;
//					}
//					else {
						SimpleDateFormat sdForm = new SimpleDateFormat(DATE_FORM_HM);
						String time = sdForm.format(System.currentTimeMillis());
						cmtBean.setTaikinTime(time);
						
						service.insertTaikin(cmtBean);
						service.insertZangyou(service.getTodayData(cmtBean));
						
						session.setAttribute("commuteMsg", "お疲れ様でした。");
						return REDIRECT_COMMUTE;
//					}
				}
				else {
					session.setAttribute("commuteMsg", "もう退勤しました。");
					return REDIRECT_COMMUTE;
				}
			}
		}
		catch(NullPointerException e) {
			return REDIRECT_LOGIN;
		}
	}
	
	@RequestMapping(value=LinksConfig.COMMUTE_INFO_LIST_LINK, method=RequestMethod.GET)
	public ModelAndView commuteList(HttpSession session, HttpServletRequest req) {
		
		ModelAndView model = new ModelAndView();
		if(null != session.getAttribute("commuteMsg")) {
			model.addObject("msg", session.getAttribute("commuteMsg"));
			session.removeAttribute("commuteMsg");
		}
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			ArrayList<Integer> yearsList = new ArrayList<Integer>();
			SimpleDateFormat yearForm = new SimpleDateFormat("yyyy");
			for(int i=2010; i<Integer.parseInt(yearForm.format(System.currentTimeMillis()))+1; i++ )
				yearsList.add(i);
			
			if(employee.getLeaderKb().equals(ENUM_KANRISYA.getValue()) || employee.getLeaderKb().equals(ENUM_GENBARIDA.getValue())) {
				model.addObject("empKubun", "kanrisya");
				CommuteMngBean mngBean = new CommuteMngBean();
				List<CommuteMngBean> list;
				String year = req.getParameter("searchYears");
				String month = req.getParameter("searchMonths");
				
				if(null != year || null != month) {
					mngBean.setDate(year.concat(month));
					list = service.getCommuteAllList(mngBean);
					model.addObject("searchYear", year);
					model.addObject("searchMonth", month);
				}
				else {
					SimpleDateFormat sdForm = new SimpleDateFormat(DATE_FORM_YM);
					String date = sdForm.format(System.currentTimeMillis());
					mngBean.setDate(date);
					list = service.getCommuteAllList(mngBean);
					model.addObject("searchYear", date.substring(0, 4));
					model.addObject("searchMonth", date.substring(4, 6));
				}
				model.addObject("list", list);
				model.addObject("yearsList", yearsList);
				model.setViewName(LOCATION_COMMUTE_MNG);
				return model;
			}
			else {
				model.setViewName(LOCATION_COMMUTE_LIST);
				CommuteBean check = new CommuteBean();
				check.setMailAddr(employee.getMailAddr());
				
				List<CommuteBean> list;
				String year = req.getParameter("searchYears");
				String month = req.getParameter("searchMonths");
				
				if(null != year || null != month) {
					check.setDate(year.concat(month));
					list = service.getCommuteList(check);
					model.addObject("searchYear", year);
					model.addObject("searchMonth", month);
				}
				else {
					SimpleDateFormat sdForm = new SimpleDateFormat(DATE_FORM_YM);
					String date = sdForm.format(System.currentTimeMillis());
					check.setDate(date);
					list = service.getCommuteList(check);
					model.addObject("searchYear", date.substring(0, 4));
					model.addObject("searchMonth", date.substring(4, 6));
				}
				double total = 0.0;
				for(int i=0; i<list.size(); i++) {
					String syukin = list.get(i).getSyukinTime();
					String taikin = list.get(i).getTaikinTime();
					int zangyou = (int)list.get(i).getZangyouJikan();
					if(null==taikin || taikin.equals("") )
						continue;
					int sTime = Integer.parseInt(syukin);
					if(sTime < 900)
						sTime = 900;
					if(sTime>1200 && sTime<1300)
						sTime = 1300;
					int tTime = Integer.parseInt(taikin);
					int sMin = (sTime/100*60) + sTime%100;
					int tMin = 0;
					if(tTime >= 1800)
						tMin = 18*60;
					else
						tMin = (tTime/100*60) + tTime%100;
					int totalMin = tMin - sMin;
					double totalHour = totalMin/60;
					if(totalMin%60 >= 30)
						totalHour += 0.5;
					total += totalHour;
					total += zangyou;
					if(sTime<1200 && tTime>1300)
						total -= 1;
				}
				model.addObject("totalTime", total);
				model.addObject("list", list);
				model.addObject("yearsList", yearsList);
				
				return model;
			}
		}
		catch(NullPointerException e) {
			model.setViewName(LOCATION_LOGIN);
			return model;
		}
		
	}
	
	@RequestMapping(value=LinksConfig.COMMUTE_INFO_UPDATE_LINK, method=RequestMethod.GET)
	public ModelAndView commuteUpdate(HttpSession session, HttpServletRequest req) {
		
		ModelAndView model = new ModelAndView();
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			if(employee.getLeaderKb().equals(ENUM_KANRISYA.getValue()) || employee.getLeaderKb().equals(ENUM_GENBARIDA.getValue()))
				model.addObject("empKubun", "kanrisya");
			CommuteBean ctBean = new CommuteBean();
			ctBean.setNo(Integer.parseInt(req.getParameter("no")));
			ctBean.setDate(req.getParameter("updateDate"));
			
			model.setViewName(LOCATION_COMMUTE_UPDATE);
			model.addObject("commute", service.getUpdateData(ctBean));
			
			return model;
		}
		catch(NullPointerException e) {
			model.setViewName(LOCATION_LOGIN);
			return model;
		}
	}
	
	@RequestMapping(value=LinksConfig.COMMUTE_INFO_UPDATE_LINK, method=RequestMethod.POST)
	public String commuteUpdateAction(HttpServletRequest req, HttpSession session) {
		try {
			CommuteBean check = new CommuteBean();
			check.setNo(Integer.parseInt(req.getParameter("no")));
			check.setDate(req.getParameter("date"));
			
			CommuteBean commute = service.getUpdateData(check);
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");		
			String skTime = req.getParameter("syukinTime");
			String tkTime = req.getParameter("taikinTime");
			
			try {
				int skTimeInt = Integer.parseInt(skTime);
				int tkTimeInt = Integer.parseInt(tkTime);
				if(skTimeInt > tkTimeInt || tkTimeInt > 2400 || skTimeInt < 700) {
					session.setAttribute("commuteMsg", "入力した時間が正しくないです。");
					return REDIRECT_COMMUTE_LIST;
				}
				
				/**
				 *遅刻修整 
				 */
//				ChikokuListBean cklBean = new ChikokuListBean();
//				cklBean.setTsuukinNo(commute.getNo());
//				
//				if(skTimeInt<=900 && null != service.getChikokuData(cklBean)) 
//					service.deleteChikoku(commute);
				
				commute.setSyukinTime(skTime);
				commute.setTaikinTime(tkTime);
				commute.setUpdateMail(employee.getMailAddr());
				service.updateTime(commute);
				service.insertZangyou(service.getUpdateData(commute));
				return REDIRECT_COMMUTE_LIST;
			}
			catch(Exception e) {
				session.setAttribute("commuteMsg", "入力した時間が正しくないです。");
				e.printStackTrace();
				return REDIRECT_COMMUTE_LIST;
			}
		}
		catch(NullPointerException e) {
			return REDIRECT_LOGIN;
		}
	}
	
	@RequestMapping(value=LinksConfig.COMMUTE_INFO_DELETE_LINK, method=RequestMethod.POST)
	public String deleteCommute(HttpServletRequest req) {
		
		try {
			CommuteBean cmtBean = new CommuteBean();
			int no = Integer.parseInt(req.getParameter("no"));
			cmtBean.setNo(no);
			
			service.deleteChikoku(cmtBean);
			service.deleteGaisyutsu(cmtBean);
			service.deleteHankyuu(cmtBean);
			service.deleteSoutai(cmtBean);
			service.deleteCommute(cmtBean);
			
			return REDIRECT_COMMUTE_LIST;
		}
		catch(Exception e) {
			return REDIRECT_LOGIN;
		}
	}
	
	
//	遅刻
	@RequestMapping(value=LinksConfig.CHIKOKU_INFO_LINK, method=RequestMethod.GET)
	public ModelAndView goChikoku(HttpSession session) {
		
		ModelAndView model = new ModelAndView();
		if(null != session.getAttribute("commuteMsg")) {
			model.addObject("msg", session.getAttribute("commuteMsg"));
			session.removeAttribute("commuteMsg");
		}
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			model.addObject("loginUser", employee);
			model.addObject("empInfo", service.getEmployeeInfo(employee));
			
			SimpleDateFormat sdForm = new SimpleDateFormat("yyyyMMdd");
			model.addObject("sinseiDate", sdForm.format(new Date()));
			
			model.setViewName(LOCATION_CHIKOKU);
			return model;
		}
		catch(NullPointerException e) {
			model.setViewName(LOCATION_LOGIN);
			return model;
		}
	}
	
	@RequestMapping(value="/management/commute/chikoku/sakusei", method=RequestMethod.POST)
	public String chikokuSakusei(HttpSession session, HttpServletRequest req) {
		
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			CommuteBean cmtBean = new CommuteBean();
			cmtBean.setMailAddr(employee.getMailAddr());
			String ckDate = req.getParameter("ckDate");
			cmtBean.setDate(ckDate.substring(0, 4).concat(ckDate.substring(5, 7).concat(ckDate.substring(8, 10))));
			
			cmtBean = service.getTsuukinData(cmtBean);
			if(null == cmtBean) {
				CommuteBean input = new CommuteBean();
				input.setMailAddr(employee.getMailAddr());
				input.setDate(ckDate.substring(0, 4).concat(ckDate.substring(5, 7).concat(ckDate.substring(8, 10))));
				input.setSyukinTime(req.getParameter("ckHour").concat(req.getParameter("ckMin")));
				input.setTaikinTime("1800");
				input.setInsertMail(employee.getMailAddr());
				input.setInsertDateTime(new Date());
				service.insertTsuukin(input);
				
				cmtBean = service.getTsuukinData(input);
			}
			else {
				cmtBean.setSyukinTime(req.getParameter("ckHour").concat(req.getParameter("ckMin")));
				cmtBean.setUpdateMail(employee.getMailAddr());
				cmtBean.setUpdateDateTime(new Date());
				service.updateTime(cmtBean);
			}
			ChikokuBean ckBean = new ChikokuBean();
			ckBean.setTsuukinNo(cmtBean.getNo());
			ckBean.setReason(req.getParameter("ckReason"));
			ckBean.setInsertMail(employee.getMailAddr());
			ckBean.setInsertDateTime(new Date());
			ckBean.setChouhyouShouninCd("01");
			ckBean.setKinntaiKb("01");
			service.insertChikoku(ckBean);
			
			return REDIRECT_CHIKOKU_LIST;
		}
		catch(NullPointerException e) {
			return REDIRECT_LOGIN;
		}
	}
	
	@RequestMapping(value="/management/commute/chikoku/yousei", method=RequestMethod.POST)
	public String chikokuYousei(HttpSession session, HttpServletRequest req) {
		
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			
			CommuteBean cmtBean = new CommuteBean();
			cmtBean.setMailAddr(employee.getMailAddr());
			String ckDate = req.getParameter("ckDate");
			cmtBean.setDate(ckDate.substring(0, 4).concat(ckDate.substring(5, 7).concat(ckDate.substring(8, 10))));
			
			cmtBean = service.getTsuukinData(cmtBean);
			if(null == cmtBean) {
				CommuteBean input = new CommuteBean();
				input.setMailAddr(employee.getMailAddr());
				input.setDate(ckDate.substring(0, 4).concat(ckDate.substring(5, 7).concat(ckDate.substring(8, 10))));
				input.setSyukinTime(req.getParameter("ckHour").concat(req.getParameter("ckMin")));
				input.setTaikinTime("1800");
				input.setInsertMail(employee.getMailAddr());
				input.setInsertDateTime(new Date());
				service.insertTsuukin(input);
				
				cmtBean = service.getTsuukinData(input);
			}
			else {
				cmtBean.setSyukinTime(req.getParameter("ckHour").concat(req.getParameter("ckMin")));
				cmtBean.setUpdateMail(employee.getMailAddr());
				cmtBean.setUpdateDateTime(new Date());
				service.updateTime(cmtBean);
			}
			
			if(null != req.getParameter("ckNo")) {
				ChikokuBean delete = new ChikokuBean();
				delete.setNo(Integer.parseInt(req.getParameter("ckNo")));
				service.deleteChikokuByPK(delete);
			}
			ChikokuBean ckBean = new ChikokuBean();
			ckBean.setTsuukinNo(cmtBean.getNo());
			ckBean.setReason(req.getParameter("ckReason"));
			ckBean.setInsertMail(employee.getMailAddr());
			ckBean.setInsertDateTime(new Date());
			ckBean.setChouhyouShouninCd("02");
			ckBean.setKinntaiKb("01");
			service.insertChikoku(ckBean);
			
			return REDIRECT_CHIKOKU_LIST;
		}
		catch(NullPointerException e) {
			return REDIRECT_LOGIN;
		}
	}
	
	@RequestMapping(value=LinksConfig.CHIKOKU_INFO_LIST_LINK, method=RequestMethod.GET)
	public ModelAndView goChikokuList(HttpSession session, HttpServletRequest req) {
		
		ModelAndView model = new ModelAndView();
		
		if(null != session.getAttribute("commuteMsg")) {
			model.addObject("msg", session.getAttribute("commuteMsg"));
			session.removeAttribute("commuteMsg");
		}
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			model.addObject("loginUser", employee);
			
			if(employee.getLeaderKb().equals(ENUM_KANRISYA.getValue()) || employee.getLeaderKb().equals("02")) {
				
				ChikokuBean ckBean = new ChikokuBean();
				List<ChikokuBean> list; 
				String year = req.getParameter("searchYears");
				String month = req.getParameter("searchMonths");
				
				if(null != year || null != month) {
					ckBean.setDate(year.concat(month));
					list = service.getChikokuAllList(ckBean);
					model.addObject("searchYear", year);
					model.addObject("searchMonth", month);
				}
				else {
					SimpleDateFormat sdForm = new SimpleDateFormat(DATE_FORM_YM);
					String date = sdForm.format(System.currentTimeMillis());
					ckBean.setDate(date);
					list = service.getChikokuAllList(ckBean);
					model.addObject("searchYear", date.substring(0, 4));
					model.addObject("searchMonth", date.substring(4, 6));
				}
				model.addObject("list", list);
			}
			else {
				CommuteBean cmtBean = new CommuteBean();
				cmtBean.setMailAddr(employee.getMailAddr());
				
				List<ChikokuBean> list; 
				String year = req.getParameter("searchYears");
				String month = req.getParameter("searchMonths");
				
				if(null != year || null != month) {
					cmtBean.setDate(year.concat(month));
					list = service.getChikokuList(cmtBean);
					model.addObject("searchYear", year);
					model.addObject("searchMonth", month);
				}
				else {
					SimpleDateFormat sdForm = new SimpleDateFormat(DATE_FORM_YM);
					String date = sdForm.format(System.currentTimeMillis());
					cmtBean.setDate(date);
					list = service.getChikokuList(cmtBean);
					model.addObject("searchYear", date.substring(0, 4));
					model.addObject("searchMonth", date.substring(4, 6));
				}
				model.addObject("list", list);
			}
			ArrayList<Integer> yearsList = new ArrayList<Integer>();
			SimpleDateFormat yearForm = new SimpleDateFormat("yyyy");
			for(int i=2010; i<Integer.parseInt(yearForm.format(System.currentTimeMillis()))+1; i++ )
				yearsList.add(i);
			model.addObject("yearsList", yearsList);
			model.setViewName(LOCATION_CHIKOKU_LIST);
			return model;
		}
		catch(NullPointerException e) {
			model.setViewName(LOCATION_LOGIN);
			return model;
		}
	}
	
	@RequestMapping(value="/management/commute/chikokuDetail/{ckNo}")
	public ModelAndView goChikokuDetail(@PathVariable int ckNo, HttpSession session) {
		
		ModelAndView model = new ModelAndView();
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			model.addObject("loginUser", employee);
			
			ChikokuBean ckBean = new ChikokuBean();
			ckBean.setNo(ckNo);
			ckBean = service.getChikokuDetail(ckBean);
			
			EmployeeBean sakuseiEmp = new EmployeeBean();
			sakuseiEmp.setMailAddr(ckBean.getMailAddr());
			model.addObject("empInfo", service.getEmployeeInfo(sakuseiEmp));
			SimpleDateFormat sdForm = new SimpleDateFormat("yyyyMMdd");
			model.addObject("sinseiDate", sdForm.format(ckBean.getInsertDateTime()));
			model.addObject("ckBean", ckBean);
			
			if(ckBean.getChouhyouShouninCd().equals("01") && ckBean.getMailAddr().equals(employee.getMailAddr())) 
				model.setViewName("/management/Commute/Chikoku_Update");
			else 
				model.setViewName("/management/Commute/Chikoku_Detail");
			return model;
		}
		catch(NullPointerException e){
			model.setViewName(LOCATION_LOGIN);
			return model;
		}
	}
	
	@RequestMapping(value=LinksConfig.CHIKOKU_INFO_UPDATE_LINK, method=RequestMethod.POST)
	public String chikokuUpdate(HttpSession session, HttpServletRequest req) {
		
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			CommuteBean cmtBean = new CommuteBean();
			cmtBean.setNo(Integer.parseInt(req.getParameter("tsuukinNo")));
			cmtBean.setDate(req.getParameter("tDate"));
			cmtBean = service.getUpdateData(cmtBean);
			cmtBean.setSyukinTime(req.getParameter("ckHour").concat(req.getParameter("ckMin")));
			cmtBean.setUpdateMail(employee.getMailAddr());
			cmtBean.setUpdateDateTime(new Date());
			service.updateTime(cmtBean);
			
			ChikokuBean ckBean = new ChikokuBean();
			ckBean.setNo(Integer.parseInt(req.getParameter("ckNo")));
			ckBean.setReason(req.getParameter("ckReason"));
			ckBean.setUpdateMail(employee.getMailAddr());
			ckBean.setUpdateDateTime(new Date());
			service.updateChikokuReason(ckBean);
			
			session.setAttribute("commuteMsg", UPDATE_MSG);
			return REDIRECT_CHIKOKU_LIST;
		}
		catch(NullPointerException e) {
			return REDIRECT_LOGIN;
		}
	}
	
	@RequestMapping(value="/management/commute/chikoku/delete")
	public String chikokuDelete(HttpServletRequest req) {
		
		try {
			ChikokuBean ckBean = new ChikokuBean();
			ckBean.setNo(Integer.parseInt(req.getParameter("ckNo")));
			service.deleteChikokuByPK(ckBean);
			
			return REDIRECT_CHIKOKU_LIST;
		}
		catch(Exception e) {
			return REDIRECT_LOGIN;
		}
	}
	
	@RequestMapping(value="/management/commute/chikoku/shounin/{ckNo}")
	public String chikokuShounin(HttpSession session, @PathVariable int ckNo) {
		
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			ChikokuBean ckBean = new ChikokuBean();
			ckBean.setNo(ckNo);
			ckBean = service.getChikokuDetail(ckBean);
			if(ckBean.getChouhyouShouninCd().equals("01") || ckBean.getChouhyouShouninCd().equals("05") 
					|| ckBean.getChouhyouShouninCd().equals("06")) {
				return "redirect:/management/commute/chikokuDetail/" + ckNo;
			}
			else {
				if(null == ckBean.getLeaderShouninMailAddr() && employee.getLeaderKb().equals("02")) {
					ckBean.setChouhyouShouninCd("04");
					ckBean.setLeaderShouninMailAddr(employee.getMailAddr());
					ckBean.setLeaderShouninDateTime(new Date());
				}
				if(null == ckBean.getKanrisyaShouninMailAddr() && employee.getLeaderKb().equals("01")){
					ckBean.setChouhyouShouninCd("05");
					ckBean.setKanrisyaShouninMailAddr(employee.getMailAddr());
					ckBean.setKanrisyaShouninDateTime(new Date());
				}
				service.chikokuKessai(ckBean);
				return "redirect:/management/commute/chikokuDetail/" + ckNo;
			}
		}
		catch(NullPointerException e) {
			return LOCATION_LOGIN;
		}
	}
	
	
//	外出
	@RequestMapping(value=LinksConfig.GAISYUTSU_INFO_LINK, method=RequestMethod.GET)
	public ModelAndView goGaisyutsu(HttpSession session) {
		
		ModelAndView model = new ModelAndView();
		if(null != session.getAttribute("commuteMsg")) {
			model.addObject("msg", session.getAttribute("commuteMsg"));
			session.removeAttribute("commuteMsg");
		}
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			model.addObject("loginUser", employee);
			model.addObject("empInfo", service.getEmployeeInfo(employee));
			
			SimpleDateFormat sdForm = new SimpleDateFormat("yyyyMMdd");
			model.addObject("sinseiDate", sdForm.format(new Date()));
			
			model.setViewName(LOCATION_GAISYUTSU);
			return model;
		}
		catch(NullPointerException e) {
			model.setViewName(LOCATION_LOGIN);
			return model;
		}
	}
	
	@RequestMapping(value="/management/commute/gaisyutsu/sakusei", method=RequestMethod.POST)
	public String gaisyutsuSakusei(HttpSession session, HttpServletRequest req) {
		
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			
			CommuteBean cmtBean = new CommuteBean();
			cmtBean.setMailAddr(employee.getMailAddr());
			String gsDate = req.getParameter("gsDate");
			cmtBean.setDate(gsDate.substring(0, 4).concat(gsDate.substring(5, 7).concat(gsDate.substring(8, 10))));
			
			cmtBean = service.getTsuukinData(cmtBean);
			if(null == cmtBean) {
				CommuteBean input = new CommuteBean();
				input.setMailAddr(employee.getMailAddr());
				input.setDate(gsDate.substring(0, 4).concat(gsDate.substring(5, 7).concat(gsDate.substring(8, 10))));
				input.setSyukinTime("0900");
				input.setTaikinTime("1800");
				input.setInsertMail(employee.getMailAddr());
				input.setInsertDateTime(new Date());
				service.insertTsuukin(input);
				
				cmtBean = service.getTsuukinData(input);
			}
			GaisyutsuBean gsBean = new GaisyutsuBean();
			gsBean.setTsuukinNo(cmtBean.getNo());
			gsBean.setStartTime(req.getParameter("gssHour").concat(req.getParameter("gssMin")));
			gsBean.setEndTime(req.getParameter("gseHour").concat(req.getParameter("gseMin")));
			gsBean.setReason(req.getParameter("gsReason"));
			gsBean.setInsertMail(employee.getMailAddr());
			gsBean.setInsertDateTime(new Date());
			gsBean.setChouhyouShouninCd("01");
			gsBean.setKinntaiKb("02");
			service.insertGaisyutsu(gsBean);
			
			return REDIRECT_GAISYUTSU_LIST;
		}
		catch(NullPointerException e) {
			return REDIRECT_LOGIN;
		}
	}
	
	@RequestMapping(value="/management/commute/gaisyutsu/yousei", method=RequestMethod.POST)
	public String gaisyutsuYousei(HttpSession session, HttpServletRequest req) {
		
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			CommuteBean cmtBean = new CommuteBean();
			cmtBean.setMailAddr(employee.getMailAddr());
			String gsDate = req.getParameter("gsDate");
			cmtBean.setDate(gsDate.substring(0, 4).concat(gsDate.substring(5, 7).concat(gsDate.substring(8, 10))));
			
			cmtBean = service.getTsuukinData(cmtBean);
			if(null == cmtBean) {
				CommuteBean input = new CommuteBean();
				input.setMailAddr(employee.getMailAddr());
				input.setDate(gsDate.substring(0, 4).concat(gsDate.substring(5, 7).concat(gsDate.substring(8, 10))));
				input.setSyukinTime("0900");
				input.setTaikinTime("1800");
				input.setInsertMail(employee.getMailAddr());
				input.setInsertDateTime(new Date());
				service.insertTsuukin(input);
				
				cmtBean = service.getTsuukinData(input);
			}
			
			if(null != req.getParameter("gsNo")) {
				GaisyutsuBean delete = new GaisyutsuBean();
				delete.setNo(Integer.parseInt(req.getParameter("gsNo")));
				service.deleteGaisyutsuByPK(delete);
			}
			GaisyutsuBean gsBean = new GaisyutsuBean();
			gsBean.setTsuukinNo(cmtBean.getNo());
			gsBean.setStartTime(req.getParameter("gssHour").concat(req.getParameter("gssMin")));
			gsBean.setEndTime(req.getParameter("gseHour").concat(req.getParameter("gseMin")));
			gsBean.setReason(req.getParameter("gsReason"));
			gsBean.setInsertMail(employee.getMailAddr());
			gsBean.setInsertDateTime(new Date());
			gsBean.setChouhyouShouninCd("02");
			gsBean.setKinntaiKb("02");
			service.insertGaisyutsu(gsBean);
			
			return REDIRECT_GAISYUTSU_LIST;
		}
		catch(NullPointerException e) {
			return REDIRECT_LOGIN;
		}
	}
	
	@RequestMapping(value=LinksConfig.GAISYUTSU_INFO_LIST_LINK, method=RequestMethod.GET)
	public ModelAndView gaisyutsuList(HttpSession session, HttpServletRequest req) {
		
		ModelAndView model = new ModelAndView();
		if(null != session.getAttribute("commuteMsg")) {
			model.addObject("msg", session.getAttribute("commuteMsg"));
			session.removeAttribute("commuteMsg");
		}
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			model.addObject("loginUser", employee);
			
			if(employee.getLeaderKb().equals(ENUM_KANRISYA.getValue()) || employee.getLeaderKb().equals("02")) {
				
				List<GaisyutsuBean> list;
				String year = req.getParameter("searchYears");
				String month = req.getParameter("searchMonths");
				GaisyutsuBean gslBean = new GaisyutsuBean();
				
				if(null != year || null != month) {
					gslBean.setDate(year.concat(month));
					list = service.getGaisyutsuAllList(gslBean);
					model.addObject("searchYear", year);
					model.addObject("searchMonth", month);
				}
				else {
					SimpleDateFormat sdForm = new SimpleDateFormat(DATE_FORM_YM);
					String date = sdForm.format(System.currentTimeMillis());
					gslBean.setDate(date);
					list = service.getGaisyutsuAllList(gslBean);
					model.addObject("searchYear", date.substring(0, 4));
					model.addObject("searchMonth", date.substring(4, 6));
				}
				model.addObject("list", list);
			}
			else {
				CommuteBean check = new CommuteBean();
				check.setMailAddr(employee.getMailAddr());
				CommuteBean cmtBean = service.getTodayData(check);
				if(null == cmtBean)
					cmtBean = check;
				List<GaisyutsuBean> list;
				String year = req.getParameter("searchYears");
				String month = req.getParameter("searchMonths");
				
				if(null != year || null != month) {
					cmtBean.setDate(year.concat(month));
					list = service.getGaisyutsuList(cmtBean);
					model.addObject("searchYear", year);
					model.addObject("searchMonth", month);
				}
				else {
					SimpleDateFormat sdForm = new SimpleDateFormat(DATE_FORM_YM);
					String date = sdForm.format(System.currentTimeMillis());
					cmtBean.setDate(date);
					list = service.getGaisyutsuList(cmtBean);
					model.addObject("searchYear", date.substring(0, 4));
					model.addObject("searchMonth", date.substring(4, 6));
				}
				model.addObject("list", list);
			}
			ArrayList<Integer> yearsList = new ArrayList<Integer>();
			SimpleDateFormat yearForm = new SimpleDateFormat("yyyy");
			for(int i=2010; i<Integer.parseInt(yearForm.format(System.currentTimeMillis()))+1; i++ )
				yearsList.add(i);
			model.addObject("yearsList", yearsList);
			model.setViewName(LOCATION_GAISYUTSU_LIST);
			return model;
		}
		catch(NullPointerException e) {
			model.setViewName(LOCATION_LOGIN);
			return model;
		}
	}
	
	@RequestMapping(value="/management/commute/gaisyutsuDetail/{gsNo}")
	public ModelAndView goGaisyutsuDetail(@PathVariable int gsNo, HttpSession session) {
		
		ModelAndView model = new ModelAndView();
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			model.addObject("loginUser", employee);
			
			GaisyutsuBean gsBean = new GaisyutsuBean();
			gsBean.setNo(gsNo);
			gsBean = service.getGaisyutsuDetail(gsBean);
			
			EmployeeBean sakuseiEmp = new EmployeeBean();
			sakuseiEmp.setMailAddr(gsBean.getMailAddr());
			model.addObject("empInfo", service.getEmployeeInfo(sakuseiEmp));
			SimpleDateFormat sdForm = new SimpleDateFormat("yyyyMMdd");
			model.addObject("sinseiDate", sdForm.format(gsBean.getInsertDateTime()));
			model.addObject("gsBean", gsBean);
			
			if(gsBean.getChouhyouShouninCd().equals("01") && gsBean.getMailAddr().equals(employee.getMailAddr())) 
				model.setViewName("/management/Commute/Gaisyutsu_Update");
			else 
				model.setViewName("/management/Commute/Gaisyutsu_Detail");
			return model;
		}
		catch(NullPointerException e) {
			return model;
		}
	}
	
	@RequestMapping(value=LinksConfig.GAISYUTSU_INFO_UPDATE_LINK, method=RequestMethod.POST)
	public String gaisyutsuUpdate(HttpSession session, HttpServletRequest req) {

		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			
			GaisyutsuBean gsBean = new GaisyutsuBean();
			gsBean.setNo(Integer.parseInt(req.getParameter("gsNo")));
			gsBean.setStartTime(req.getParameter("gssHour").concat(req.getParameter("gssMin")));
			gsBean.setEndTime(req.getParameter("gseHour").concat(req.getParameter("gseMin")));
			gsBean.setReason(req.getParameter("gsReason"));
			gsBean.setUpdateMail(employee.getMailAddr());
			gsBean.setUpdateDateTime(new Date());
			service.updateGaisyutsu(gsBean);
			
			session.setAttribute("commuteMsg", UPDATE_MSG);
			return REDIRECT_GAISYUTSU_LIST;
		}
		catch(NullPointerException e) {
			return REDIRECT_LOGIN;
		}
	}
	
	@RequestMapping(value="/management/commute/gaisyutsu/delete")
	public String gaisyutsuDelete(HttpServletRequest req) {
		
		try {
			GaisyutsuBean gsBean = new GaisyutsuBean();
			gsBean.setNo(Integer.parseInt(req.getParameter("gsNo")));
			service.deleteGaisyutsuByPK(gsBean);
			
			return REDIRECT_GAISYUTSU_LIST;
		}
		catch(Exception e) {
			return REDIRECT_LOGIN;
		}
	}
	
	@RequestMapping(value="/management/commute/gaisyutsu/shounin/{gsNo}")
	public String gaisyutsuShounin(HttpSession session, @PathVariable int gsNo) {
		
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			GaisyutsuBean gsBean = new GaisyutsuBean();
			gsBean.setNo(gsNo);
			gsBean = service.getGaisyutsuDetail(gsBean);
			if(gsBean.getChouhyouShouninCd().equals("01") || gsBean.getChouhyouShouninCd().equals("05") 
					|| gsBean.getChouhyouShouninCd().equals("06")) {
				return "redirect:/management/commute/gaisyutsuDetail/" + gsNo;
			}
			else {
				if(null == gsBean.getLeaderShouninMailAddr() && employee.getLeaderKb().equals("02")) {
					gsBean.setChouhyouShouninCd("04");
					gsBean.setLeaderShouninMailAddr(employee.getMailAddr());
					gsBean.setLeaderShouninDateTime(new Date());
				}
				if(null == gsBean.getKanrisyaShouninMailAddr() && employee.getLeaderKb().equals("01")){
					gsBean.setChouhyouShouninCd("05");
					gsBean.setKanrisyaShouninMailAddr(employee.getMailAddr());
					gsBean.setKanrisyaShouninDateTime(new Date());
				}
				service.gaisyutsuKessai(gsBean);
				return "redirect:/management/commute/gaisyutsuDetail/" + gsNo;
			}
		}
		catch(NullPointerException e) {
			return LOCATION_LOGIN;
		}
	}
	
	
//	早退
	@RequestMapping(value=LinksConfig.SOUTAI_INFO_LINK, method=RequestMethod.GET)
	public ModelAndView goSoutai(HttpSession session) {

		ModelAndView model = new ModelAndView();
		if(null != session.getAttribute("commuteMsg")) {
			model.addObject("msg", session.getAttribute("commuteMsg"));
			session.removeAttribute("commuteMsg");
		}
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			model.addObject("loginUser", employee);
			model.addObject("empInfo", service.getEmployeeInfo(employee));
			
			SimpleDateFormat sdForm = new SimpleDateFormat("yyyyMMdd");
			model.addObject("sinseiDate", sdForm.format(new Date()));
			
			model.setViewName(LOCATION_SOUTAI);
			return model;
		}
		catch(NullPointerException e) {
			model.setViewName(LOCATION_LOGIN);
			return model;
		}
	}
	
	@RequestMapping(value="/management/commute/soutai/sakusei", method=RequestMethod.POST)
	public String soutaiSakusei(HttpServletRequest req, HttpSession session){
		
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			
			CommuteBean cmtBean = new CommuteBean();
			cmtBean.setMailAddr(employee.getMailAddr());
			String stDate = req.getParameter("stDate");
			cmtBean.setDate(stDate.substring(0, 4).concat(stDate.substring(5, 7).concat(stDate.substring(8, 10))));
			
			cmtBean = service.getTsuukinData(cmtBean);
			if(null == cmtBean) {
				CommuteBean input = new CommuteBean();
				input.setMailAddr(employee.getMailAddr());
				input.setDate(stDate.substring(0, 4).concat(stDate.substring(5, 7).concat(stDate.substring(8, 10))));
				input.setSyukinTime("0900");
				input.setTaikinTime(req.getParameter("stHour").concat(req.getParameter("stMin")));
				input.setInsertMail(employee.getMailAddr());
				input.setInsertDateTime(new Date());
				service.insertTsuukin(input);
				
				cmtBean = service.getTsuukinData(input);
			}
			else {
				cmtBean.setTaikinTime(req.getParameter("stHour").concat(req.getParameter("stMin")));
				cmtBean.setUpdateMail(employee.getMailAddr());
				cmtBean.setUpdateDateTime(new Date());
				service.updateTime(cmtBean);
			}
			SoutaiBean stBean = new SoutaiBean();
			stBean.setTsuukinNo(cmtBean.getNo());
			stBean.setReason(req.getParameter("stReason"));
			stBean.setInsertMail(employee.getMailAddr());
			stBean.setInsertDateTime(new Date());
			stBean.setChouhyouShouninCd("01");
			stBean.setKinntaiKb("03");
			service.insertSoutai(stBean);
			
			return REDIRECT_SOUTAI_LIST;
		}
		catch(NullPointerException e) {
			return REDIRECT_LOGIN;
		}
	}
	
	@RequestMapping(value="/management/commute/soutai/yousei", method=RequestMethod.POST)
	public String soutaiYousei(HttpSession session, HttpServletRequest req) {
		
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			CommuteBean cmtBean = new CommuteBean();
			cmtBean.setMailAddr(employee.getMailAddr());
			String stDate = req.getParameter("stDate");
			cmtBean.setDate(stDate.substring(0, 4).concat(stDate.substring(5, 7).concat(stDate.substring(8, 10))));
			
			cmtBean = service.getTsuukinData(cmtBean);
			if(null == cmtBean) {
				CommuteBean input = new CommuteBean();
				input.setMailAddr(employee.getMailAddr());
				input.setDate(stDate.substring(0, 4).concat(stDate.substring(5, 7).concat(stDate.substring(8, 10))));
				input.setSyukinTime("0900");
				input.setTaikinTime(req.getParameter("stHour").concat(req.getParameter("stMin")));
				input.setInsertMail(employee.getMailAddr());
				input.setInsertDateTime(new Date());
				service.insertTsuukin(input);
				
				cmtBean = service.getTsuukinData(input);
			}
			else {
				cmtBean.setTaikinTime(req.getParameter("stHour").concat(req.getParameter("stMin")));
				cmtBean.setUpdateMail(employee.getMailAddr());
				cmtBean.setUpdateDateTime(new Date());
				service.updateTime(cmtBean);
			}
			
			if(null != req.getParameter("stNo")) {
				SoutaiBean delete = new SoutaiBean();
				delete.setNo(Integer.parseInt(req.getParameter("stNo")));
				service.deleteSoutaiByPK(delete);
			}
			SoutaiBean stBean = new SoutaiBean();
			stBean.setTsuukinNo(cmtBean.getNo());
			stBean.setReason(req.getParameter("stReason"));
			stBean.setInsertMail(employee.getMailAddr());
			stBean.setInsertDateTime(new Date());
			stBean.setChouhyouShouninCd("02");
			stBean.setKinntaiKb("03");
			service.insertSoutai(stBean);
			
			return REDIRECT_SOUTAI_LIST;
		}
		catch(NullPointerException e) {
			return REDIRECT_LOGIN;
		}
	}
	
	@RequestMapping(value=LinksConfig.SOUTAI_INFO_LIST_LINK, method=RequestMethod.GET)
	public ModelAndView soutaiList(HttpSession session, HttpServletRequest req) {
		
		EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
		
		ModelAndView model = new ModelAndView();
		if(null != session.getAttribute("commuteMsg")) {
			model.addObject("msg", session.getAttribute("commuteMsg"));
			session.removeAttribute("commuteMsg");
		}
		try {
			ArrayList<Integer> yearsList = new ArrayList<Integer>();
			SimpleDateFormat yearForm = new SimpleDateFormat("yyyy");
			for(int i=2010; i<Integer.parseInt(yearForm.format(System.currentTimeMillis()))+1; i++ )
				yearsList.add(i);
			
			if(employee.getLeaderKb().equals(ENUM_KANRISYA.getValue()) || employee.getLeaderKb().equals(ENUM_GENBARIDA.getValue())) {
				
				model.addObject("empKubun", "kanrisya");
				model.setViewName(LOCATION_SOUTAI_LIST);
				SoutaiBean stBean = new SoutaiBean();
				List<SoutaiBean> list;
				String year = req.getParameter("searchYears");
				String month = req.getParameter("searchMonths");
				
				if(null != year || null != month) {
					stBean.setDate(year.concat(month));
					list = service.getSoutaiAllList(stBean);
					model.addObject("searchYear", year);
					model.addObject("searchMonth", month);
				}
				else {
					SimpleDateFormat sdForm = new SimpleDateFormat(DATE_FORM_YM);
					String date = sdForm.format(System.currentTimeMillis());
					stBean.setDate(date);
					list = service.getSoutaiAllList(stBean);
					model.addObject("searchYear", date.substring(0, 4));
					model.addObject("searchMonth", date.substring(4, 6));
				}
				model.addObject("list", list);
				model.addObject("yearsList", yearsList);
				return model;
			}
			else {
				model.setViewName(LOCATION_SOUTAI_LIST);
				CommuteBean check = new CommuteBean();
				check.setMailAddr(employee.getMailAddr());
				CommuteBean cmtBean = service.getTodayData(check);
				if(null == cmtBean)
					cmtBean = check;
				List<SoutaiBean> list;
				String year = req.getParameter("searchYears");
				String month = req.getParameter("searchMonths");
				
				if(null != year || null != month) {
					cmtBean.setDate(year.concat(month));
					list = service.getSoutaiList(cmtBean);
					model.addObject("searchYear", year);
					model.addObject("searchMonth", month);
				}
				else {
					SimpleDateFormat sdForm = new SimpleDateFormat(DATE_FORM_YM);
					String date = sdForm.format(System.currentTimeMillis());
					cmtBean.setDate(date);
					list = service.getSoutaiList(cmtBean);
					model.addObject("searchYear", date.substring(0, 4));
					model.addObject("searchMonth", date.substring(4, 6));
				}
				model.addObject("list", list);
				model.addObject("yearsList", yearsList);
				return model;
			}
		}
		catch(NullPointerException e) {
			model.setViewName(LOCATION_LOGIN);
			return model;
		}
	}
	
	@RequestMapping(value="/management/commute/soutaiDetail/{stNo}")
	public ModelAndView goSoutaiDetail(@PathVariable int stNo, HttpSession session) {
		
		ModelAndView model = new ModelAndView();
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			model.addObject("loginUser", employee);
			
			SoutaiBean stBean = new SoutaiBean();
			stBean.setNo(stNo);
			stBean = service.getSoutaiDetail(stBean);
			
			EmployeeBean sakuseiEmp = new EmployeeBean();
			sakuseiEmp.setMailAddr(stBean.getMailAddr());
			model.addObject("empInfo", service.getEmployeeInfo(sakuseiEmp));
			SimpleDateFormat sdForm = new SimpleDateFormat("yyyyMMdd");
			model.addObject("sinseiDate", sdForm.format(stBean.getInsertDateTime()));
			model.addObject("stBean", stBean);
			
			if(stBean.getChouhyouShouninCd().equals("01") && stBean.getMailAddr().equals(employee.getMailAddr())) 
				model.setViewName("/management/Commute/Soutai_Update");
			else 
				model.setViewName("/management/Commute/Soutai_Detail");
			return model;
		}
		catch(NullPointerException e) {
			return model;
		}
	}
	
	@RequestMapping(value=LinksConfig.SOUTAI_INFO_UPDATE_LINK, method=RequestMethod.POST)
	public String soutaiUpdate(HttpSession session, HttpServletRequest req) {
		
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			CommuteBean cmtBean = new CommuteBean();
			cmtBean.setNo(Integer.parseInt(req.getParameter("tsuukinNo")));
			cmtBean.setDate(req.getParameter("tDate"));
			cmtBean = service.getUpdateData(cmtBean);
			cmtBean.setTaikinTime(req.getParameter("stHour").concat(req.getParameter("stMin")));
			cmtBean.setUpdateMail(employee.getMailAddr());
			cmtBean.setUpdateDateTime(new Date());
			service.updateTime(cmtBean);
			
			SoutaiBean stBean = new SoutaiBean();
			stBean.setNo(Integer.parseInt(req.getParameter("stNo")));
			stBean.setReason(req.getParameter("stReason"));
			stBean.setUpdateMail(employee.getMailAddr());
			stBean.setUpdateDateTime(new Date());
			service.updateSoutai(stBean);
			
			session.setAttribute("commuteMsg", UPDATE_MSG);
			return REDIRECT_SOUTAI_LIST;
		}
		catch(NullPointerException e) {
			return REDIRECT_LOGIN;
		}
	}
	
	@RequestMapping(value="/management/commute/soutai/delete")
	public String soutaiDelete(HttpServletRequest req) {
		
		try {
			SoutaiBean stBean = new SoutaiBean();
			stBean.setNo(Integer.parseInt(req.getParameter("stNo")));
			service.deleteSoutaiByPK(stBean);
			
			return REDIRECT_SOUTAI_LIST;
		}
		catch(Exception e) {
			return REDIRECT_LOGIN;
		}
	}
	
	@RequestMapping(value="/management/commute/soutai/shounin/{stNo}")
	public String soutaiShounin(HttpSession session, @PathVariable int stNo) {
		
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			SoutaiBean stBean = new SoutaiBean();
			stBean.setNo(stNo);
			stBean = service.getSoutaiDetail(stBean);
			if(stBean.getChouhyouShouninCd().equals("01") || stBean.getChouhyouShouninCd().equals("05") 
					|| stBean.getChouhyouShouninCd().equals("06")) {
				return "redirect:/management/commute/soutaiDetail/" + stNo;
			}
			else {
				if(null == stBean.getLeaderShouninMailAddr() && employee.getLeaderKb().equals("02")) {
					stBean.setChouhyouShouninCd("04");
					stBean.setLeaderShouninMailAddr(employee.getMailAddr());
					stBean.setLeaderShouninDateTime(new Date());
				}
				if(null == stBean.getKanrisyaShouninMailAddr() && employee.getLeaderKb().equals("01")){
					stBean.setChouhyouShouninCd("05");
					stBean.setKanrisyaShouninMailAddr(employee.getMailAddr());
					stBean.setKanrisyaShouninDateTime(new Date());
				}
				service.soutaiKessai(stBean);
				return "redirect:/management/commute/soutaiDetail/" + stNo;
			}
		}
		catch(NullPointerException e) {
			return LOCATION_LOGIN;
		}
	}
	
	
//	半休
	@RequestMapping(value=LinksConfig.HANKYUU_INFO_LINK, method=RequestMethod.GET)
	public ModelAndView goHankyuu(HttpSession session) {
		
		ModelAndView model = new ModelAndView();
		if(null != session.getAttribute("commuteMsg")) {
			model.addObject("msg", session.getAttribute("commuteMsg"));
			session.removeAttribute("commuteMsg");
		}
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			model.addObject("loginUser", employee);
			model.addObject("empInfo", service.getEmployeeInfo(employee));
			
			SimpleDateFormat sdForm = new SimpleDateFormat("yyyyMMdd");
			model.addObject("sinseiDate", sdForm.format(new Date()));
			
			model.setViewName(LOCATION_HANKYUU);
			return model;
		}
		catch(NullPointerException e) {
			model.setViewName(LOCATION_LOGIN);
			return model;
		}
	}
	
	@RequestMapping(value="/management/commute/hankyuu/sakusei", method=RequestMethod.POST)
	public String hankyuuSakusei(HttpSession session, HttpServletRequest req) {
		
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			String hkZikan = req.getParameter("hkZikan");
			
			CommuteBean cmtBean = new CommuteBean();
			cmtBean.setMailAddr(employee.getMailAddr());
			String hkDate = req.getParameter("hkDate");
			cmtBean.setDate(hkDate.substring(0, 4).concat(hkDate.substring(5, 7).concat(hkDate.substring(8, 10))));
			
			cmtBean = service.getTsuukinData(cmtBean);
			if(null == cmtBean) {
				CommuteBean input = new CommuteBean();
				input.setMailAddr(employee.getMailAddr());
				input.setDate(hkDate.substring(0, 4).concat(hkDate.substring(5, 7).concat(hkDate.substring(8, 10))));
				
				if(hkZikan.equals("01")) {
					input.setSyukinTime("1300");
					input.setTaikinTime("1800");
				}
				else {
					input.setSyukinTime("0900");
					input.setTaikinTime("1200");
				}
				input.setInsertMail(employee.getMailAddr());
				input.setInsertDateTime(new Date());
				service.insertTsuukin(input);
				
				cmtBean = service.getTsuukinData(input);
			}
			else {
				if(hkZikan.equals("01")) {
					cmtBean.setSyukinTime("1300");
					cmtBean.setTaikinTime("1800");
				}
				else {
					cmtBean.setSyukinTime("0900");
					cmtBean.setTaikinTime("1200");
				}
				cmtBean.setUpdateMail(employee.getMailAddr());
				cmtBean.setUpdateDateTime(new Date());
				service.updateTime(cmtBean);
			}
			HankyuuBean hkBean = new HankyuuBean();
			hkBean.setTsuukinNo(cmtBean.getNo());
			hkBean.setJikantaiKb(hkZikan);
			hkBean.setReason(req.getParameter("hkReason"));
			hkBean.setInsertMail(employee.getMailAddr());
			hkBean.setInsertDateTime(new Date());
			hkBean.setChouhyouShouninCd("01");
			hkBean.setKinntaiKb("04");
			service.insertHankyuu(hkBean);
			
			return REDIRECT_HANKYUU_LIST;
		}
		catch(NullPointerException e) {
			return REDIRECT_LOGIN;
		}
	}
	
	@RequestMapping(value="/management/commute/hankyuu/yousei", method=RequestMethod.POST)
	public String hankyuuYousei(HttpSession session, HttpServletRequest req) {
		
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			String hkZikan = req.getParameter("hkZikan");
			
			CommuteBean cmtBean = new CommuteBean();
			cmtBean.setMailAddr(employee.getMailAddr());
			String hkDate = req.getParameter("hkDate");
			cmtBean.setDate(hkDate.substring(0, 4).concat(hkDate.substring(5, 7).concat(hkDate.substring(8, 10))));
			
			cmtBean = service.getTsuukinData(cmtBean);
			if(null == cmtBean) {
				CommuteBean input = new CommuteBean();
				input.setMailAddr(employee.getMailAddr());
				input.setDate(hkDate.substring(0, 4).concat(hkDate.substring(5, 7).concat(hkDate.substring(8, 10))));
				
				if(hkZikan.equals("01")) {
					input.setSyukinTime("1300");
					input.setTaikinTime("1800");
				}
				else {
					input.setSyukinTime("0900");
					input.setTaikinTime("1200");
				}
				input.setInsertMail(employee.getMailAddr());
				input.setInsertDateTime(new Date());
				service.insertTsuukin(input);
				
				cmtBean = service.getTsuukinData(input);
			}
			else {
				if(hkZikan.equals("01")) {
					cmtBean.setSyukinTime("1300");
					cmtBean.setTaikinTime("1800");
				}
				else {
					cmtBean.setSyukinTime("0900");
					cmtBean.setTaikinTime("1200");
				}
				cmtBean.setUpdateMail(employee.getMailAddr());
				cmtBean.setUpdateDateTime(new Date());
				service.updateTime(cmtBean);
			}
			
			if(null != req.getParameter("hkNo")) {
				HankyuuBean delete = new HankyuuBean();
				delete.setNo(Integer.parseInt(req.getParameter("hkNo")));
				service.deleteHankyuuByPK(delete);
			}
			HankyuuBean hkBean = new HankyuuBean();
			hkBean.setTsuukinNo(cmtBean.getNo());
			hkBean.setJikantaiKb(hkZikan);
			hkBean.setReason(req.getParameter("hkReason"));
			hkBean.setInsertMail(employee.getMailAddr());
			hkBean.setInsertDateTime(new Date());
			hkBean.setChouhyouShouninCd("02");
			hkBean.setKinntaiKb("04");
			service.insertHankyuu(hkBean);
			
			return REDIRECT_HANKYUU_LIST;
		}
		catch(NullPointerException e) {
			return REDIRECT_LOGIN;
		}
	}
	
	@RequestMapping(value=LinksConfig.HANKYUU_INFO_LIST_LINK, method=RequestMethod.GET)
	public ModelAndView hankyuuList(HttpSession session, HttpServletRequest req) {
		
		ModelAndView model = new ModelAndView();
		if(null != session.getAttribute("commuteMsg")) {
			model.addObject("msg", session.getAttribute("commuteMsg"));
			session.removeAttribute("commuteMsg");
		}
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			ArrayList<Integer> yearsList = new ArrayList<Integer>();
			SimpleDateFormat yearForm = new SimpleDateFormat("yyyy");
			for(int i=2010; i<Integer.parseInt(yearForm.format(System.currentTimeMillis()))+1; i++ )
				yearsList.add(i);
			
			if(employee.getLeaderKb().equals(ENUM_KANRISYA.getValue()) || employee.getLeaderKb().equals(ENUM_GENBARIDA.getValue())) {
				model.setViewName(LOCATION_HANKYUU_LIST);
				model.addObject("empKubun", "kanrisya");
				
				List<HankyuuBean> list;
				String year = req.getParameter("searchYears");
				String month = req.getParameter("searchMonths");
				HankyuuBean hkBean = new HankyuuBean();
				
				if(null != year || null != month) {
					hkBean.setDate(year.concat(month));
					list = service.getHankyuuAllList(hkBean);
					model.addObject("searchYear", year);
					model.addObject("searchMonth", month);
				}
				else {
					SimpleDateFormat sdForm = new SimpleDateFormat(DATE_FORM_YM);
					String date = sdForm.format(System.currentTimeMillis());
					hkBean.setDate(date);
					list = service.getHankyuuAllList(hkBean);
					model.addObject("searchYear", date.substring(0, 4));
					model.addObject("searchMonth", date.substring(4, 6));
				}
				model.addObject("list", list);
				model.addObject("yearsList", yearsList);
				return model;
			}
			else {
				model.setViewName(LOCATION_HANKYUU_LIST);
				
				CommuteBean check = new CommuteBean();
				check.setMailAddr(employee.getMailAddr());
				CommuteBean cmtBean = service.getTodayData(check);
				if(null == cmtBean)
					cmtBean = check;
				List<HankyuuBean> list;
				String year = req.getParameter("searchYears");
				String month = req.getParameter("searchMonths");
				
				if(null != year || null != month) {
					cmtBean.setDate(year.concat(month));
					list = service.getHankyuuList(cmtBean);
					model.addObject("searchYear", year);
					model.addObject("searchMonth", month);
				}
				else {
					SimpleDateFormat sdForm = new SimpleDateFormat(DATE_FORM_YM);
					String date = sdForm.format(System.currentTimeMillis());
					cmtBean.setDate(date);
					list = service.getHankyuuList(cmtBean);
					model.addObject("searchYear", date.substring(0, 4));
					model.addObject("searchMonth", date.substring(4, 6));
				}
				model.addObject("list", list);
				model.addObject("yearsList", yearsList);
				return model;
			}
		}
		catch(NullPointerException e) {
			model.setViewName(LOCATION_LOGIN);
			return model;
		}
	}
	
	@RequestMapping(value="/management/commute/hankyuuDetail/{hkNo}")
	public ModelAndView goHankyuuDetail(@PathVariable int hkNo, HttpSession session) {
		
		ModelAndView model = new ModelAndView();
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			model.addObject("loginUser", employee);
			
			HankyuuBean hkBean = new HankyuuBean();
			hkBean.setNo(hkNo);
			hkBean = service.getHankyuuDetail(hkBean);
			
			EmployeeBean sakuseiEmp = new EmployeeBean();
			sakuseiEmp.setMailAddr(hkBean.getMailAddr());
			model.addObject("empInfo", service.getEmployeeInfo(sakuseiEmp));
			SimpleDateFormat sdForm = new SimpleDateFormat("yyyyMMdd");
			model.addObject("sinseiDate", sdForm.format(hkBean.getInsertDateTime()));
			model.addObject("hkBean", hkBean);
			
			if(hkBean.getChouhyouShouninCd().equals("01") && hkBean.getMailAddr().equals(employee.getMailAddr())) 
				model.setViewName("/management/Commute/Hankyuu_Update");
			else 
				model.setViewName("/management/Commute/Hankyuu_Detail");
			return model;
		}
		catch(NullPointerException e) {
			return model;
		}
	}
	
	@RequestMapping(value=LinksConfig.HANKYUU_INFO_UPDATE_LINK, method=RequestMethod.POST)
	public String hankyuuUpdate(HttpSession session, HttpServletRequest req) {
		
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			String hkZikan = req.getParameter("hkZikan");
			
			CommuteBean cmtBean = new CommuteBean();
			cmtBean.setNo(Integer.parseInt(req.getParameter("tsuukinNo")));
			cmtBean.setDate(req.getParameter("tDate"));
			cmtBean = service.getUpdateData(cmtBean);
			if(hkZikan.equals("01")) {
				cmtBean.setSyukinTime("1300");
				cmtBean.setTaikinTime("1800");
			}
			else {
				cmtBean.setSyukinTime("0900");
				cmtBean.setTaikinTime("1200");
			}
			cmtBean.setUpdateMail(employee.getMailAddr());
			cmtBean.setUpdateDateTime(new Date());
			service.updateTime(cmtBean);
			
			HankyuuBean hkBean = new HankyuuBean();
			hkBean.setNo(Integer.parseInt(req.getParameter("hkNo")));
			hkBean.setJikantaiKb(hkZikan);
			hkBean.setReason(req.getParameter("hkReason"));
			hkBean.setUpdateMail(employee.getMailAddr());
			hkBean.setUpdateDateTime(new Date());
			service.updateHankyuu(hkBean);
			
			session.setAttribute("commuteMsg", UPDATE_MSG);
			return REDIRECT_HANKYUU_LIST;
		}
		catch(NullPointerException e) {
			return REDIRECT_LOGIN;
		}
	}
	
	@RequestMapping(value="/management/commute/hankyuu/delete")
	public String hankyuuDelete(HttpServletRequest req) {
		
		try {
			HankyuuBean hkBean = new HankyuuBean();
			hkBean.setNo(Integer.parseInt(req.getParameter("hkNo")));
			service.deleteHankyuuByPK(hkBean);
			
			return REDIRECT_HANKYUU_LIST;
		}
		catch(Exception e) {
			return REDIRECT_LOGIN;
		}
	}
	
	@RequestMapping(value="/management/commute/hankyuu/shounin/{hkNo}")
	public String hankyuuShounin(HttpSession session, @PathVariable int hkNo) {
		
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			HankyuuBean hkBean = new HankyuuBean();
			hkBean.setNo(hkNo);
			hkBean = service.getHankyuuDetail(hkBean);
			if(hkBean.getChouhyouShouninCd().equals("01") || hkBean.getChouhyouShouninCd().equals("05") 
					|| hkBean.getChouhyouShouninCd().equals("06")) {
				return "redirect:/management/commute/hankyuuDetail/" + hkNo;
			}
			else {
				if(null == hkBean.getLeaderShouninMailAddr() && employee.getLeaderKb().equals("02")) {
					hkBean.setChouhyouShouninCd("04");
					hkBean.setLeaderShouninMailAddr(employee.getMailAddr());
					hkBean.setLeaderShouninDateTime(new Date());
				}
				if(null == hkBean.getKanrisyaShouninMailAddr() && employee.getLeaderKb().equals("01")){
					hkBean.setChouhyouShouninCd("05");
					hkBean.setKanrisyaShouninMailAddr(employee.getMailAddr());
					hkBean.setKanrisyaShouninDateTime(new Date());
				}
				service.hankyuuKessai(hkBean);
				return "redirect:/management/commute/hankyuuDetail/" + hkNo;
			}
		}
		catch(NullPointerException e) {
			return LOCATION_LOGIN;
		}
	}
	
	
//	休暇
	@RequestMapping(value=LinksConfig.KYUUKA_INFO_LINK, method=RequestMethod.GET)
	public ModelAndView goKyuuka(HttpSession session) {
		
		ModelAndView model = new ModelAndView();
		if(null != session.getAttribute("commuteMsg")) {
			model.addObject("msg", session.getAttribute("commuteMsg"));
			session.removeAttribute("commuteMsg");
		}
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			model.addObject("loginUser", employee);
			model.addObject("empInfo", service.getEmployeeInfo(employee));
			
			SimpleDateFormat sdForm = new SimpleDateFormat("yyyyMMdd");
			model.addObject("sinseiDate", sdForm.format(new Date()));
			
			model.setViewName(LOCATION_KYUUKA);
			return model;
		}
		catch(NullPointerException e) {
			model.setViewName(LOCATION_LOGIN);
			return model;
		}
	}
	
	@RequestMapping(value="/management/commute/kyuuka/sakusei", method=RequestMethod.POST)
	public String kyuukaSakusei(HttpSession session, HttpServletRequest req) {
		
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			String stDate = req.getParameter("kkaSdate");
			String edDate = req.getParameter("kkaEdate");
			
			KyuukaBean kkBean = new KyuukaBean();
			kkBean.setMailAddr(employee.getMailAddr());
			kkBean.setStartDate(stDate.substring(0, 4).concat(stDate.substring(5, 7).concat(stDate.substring(8, 10))));
			kkBean.setEndDate(edDate.substring(0, 4).concat(edDate.substring(5, 7).concat(edDate.substring(8, 10))));
			kkBean.setNaiyou(req.getParameter("kkaNaiyou"));
			kkBean.setYukisaki(req.getParameter("yukisaki"));
			kkBean.setHijouTel(req.getParameter("hijouTel"));
			kkBean.setRiyuu(req.getParameter("kkaReason"));
			SimpleDateFormat sdForm = new SimpleDateFormat("yyyyMMdd");
			kkBean.setSinseiDate(sdForm.format(new Date()));
			kkBean.setInsertMail(employee.getMailAddr());
			kkBean.setInsertDateTime(new Date());
			kkBean.setChouhyouShouninCd("01");
			kkBean.setKinntaiKb("05");
			service.insertKyuuka(kkBean);
			
			return REDIRECT_KYUUKA_LIST;
		}
		catch(NullPointerException e) {
			e.printStackTrace();
			return REDIRECT_LOGIN;
		}
	}
	
	@RequestMapping(value="/management/commute/kyuuka/yousei", method=RequestMethod.POST)
	public String kyuukaYousei(HttpSession session, HttpServletRequest req) {
		
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			
			if(null != req.getParameter("kkaNo")) {
				KyuukaBean delete = new KyuukaBean();
				delete.setNo(Integer.parseInt(req.getParameter("kkaNo")));
				service.deleteKyuuka(delete);
			}
			String stDate = req.getParameter("kkaSdate");
			String edDate = req.getParameter("kkaEdate");
			
			KyuukaBean kkBean = new KyuukaBean();
			kkBean.setMailAddr(employee.getMailAddr());
			kkBean.setStartDate(stDate.substring(0, 4).concat(stDate.substring(5, 7).concat(stDate.substring(8, 10))));
			kkBean.setEndDate(edDate.substring(0, 4).concat(edDate.substring(5, 7).concat(edDate.substring(8, 10))));
			kkBean.setNaiyou(req.getParameter("kkaNaiyou"));
			kkBean.setYukisaki(req.getParameter("yukisaki"));
			kkBean.setHijouTel(req.getParameter("hijouTel"));
			kkBean.setRiyuu(req.getParameter("kkaReason"));
			SimpleDateFormat sdForm = new SimpleDateFormat("yyyyMMdd");
			kkBean.setSinseiDate(sdForm.format(new Date()));
			kkBean.setInsertMail(employee.getMailAddr());
			kkBean.setInsertDateTime(new Date());
			kkBean.setChouhyouShouninCd("02");
			kkBean.setKinntaiKb("05");
			service.insertKyuuka(kkBean);
			
			return REDIRECT_KYUUKA_LIST;
		}
		catch(NullPointerException e) {
			return REDIRECT_LOGIN;
		}
	}
	
	@RequestMapping(value=LinksConfig.KYUUKA_INFO_LIST_LINK, method=RequestMethod.GET)
	public ModelAndView goKyuukaList(HttpSession session, HttpServletRequest req) {
		
		ModelAndView model = new ModelAndView();
		if(null != session.getAttribute("commuteMsg")) {
			model.addObject("msg", session.getAttribute("commuteMsg"));
			session.removeAttribute("commuteMsg");
		}
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			model.addObject("loginUser", employee);
			
			List<KyuukaBean> list;
			KyuukaBean kkBean = new KyuukaBean();
			
			if(employee.getLeaderKb().equals(ENUM_KANRISYA.getValue()) || employee.getLeaderKb().equals(ENUM_GENBARIDA.getValue())) {
				
				String year = req.getParameter("searchYears");
				String month = req.getParameter("searchMonths");
				
				if(null != year || null != month) {
					kkBean.setStartDate(year.concat(month));
					list = service.getKyuukaAllList(kkBean);
					model.addObject("searchYear", year);
					model.addObject("searchMonth", month);
				}
				else {
					SimpleDateFormat sdForm = new SimpleDateFormat(DATE_FORM_YM);
					String date = sdForm.format(System.currentTimeMillis());
					kkBean.setStartDate(date);
					list = service.getKyuukaAllList(kkBean);
					model.addObject("searchYear", date.substring(0, 4));
					model.addObject("searchMonth", date.substring(4, 6));
				}
				model.addObject("list", list);
			}
			else {
				kkBean.setMailAddr(employee.getMailAddr());
				
				String year = req.getParameter("searchYears");
				String month = req.getParameter("searchMonths");
				
				if(null != year || null != month) {
					kkBean.setStartDate(year.concat(month));
					list = service.getKyuukaList(kkBean);
					model.addObject("searchYear", year);
					model.addObject("searchMonth", month);
				}
				else {
					SimpleDateFormat sdForm = new SimpleDateFormat(DATE_FORM_YM);
					String date = sdForm.format(System.currentTimeMillis());
					kkBean.setStartDate(date);
					list = service.getKyuukaList(kkBean);
					model.addObject("searchYear", date.substring(0, 4));
					model.addObject("searchMonth", date.substring(4, 6));
				}
				model.addObject("list", list);
			}
			ArrayList<Integer> yearsList = new ArrayList<Integer>();
			SimpleDateFormat yearForm = new SimpleDateFormat("yyyy");
			for(int i=2010; i<Integer.parseInt(yearForm.format(System.currentTimeMillis()))+1; i++ )
				yearsList.add(i);
			model.addObject("yearsList", yearsList);
			model.setViewName(LOCATION_KYUUKA_LIST);
			return model;
		}
		catch(NullPointerException e) {
			model.setViewName(LOCATION_LOGIN);
			return model;
		}
	}
	
	@RequestMapping(value="/management/commute/kyuukaDetail/{kkNo}")
	public ModelAndView goKyuukaDetail(@PathVariable int kkNo, HttpSession session) {
		
		ModelAndView model = new ModelAndView();
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			model.addObject("loginUser", employee);
			
			KyuukaBean kkBean = new KyuukaBean();
			kkBean.setNo(kkNo);
			kkBean = service.getKyuukaDetail(kkBean);
			
			EmployeeBean sakuseiEmp = new EmployeeBean();
			sakuseiEmp.setMailAddr(kkBean.getMailAddr());
			model.addObject("empInfo", service.getEmployeeInfo(sakuseiEmp));
			SimpleDateFormat sdForm = new SimpleDateFormat("yyyyMMdd");
			model.addObject("sinseiDate", sdForm.format(kkBean.getInsertDateTime()));
			model.addObject("kkaBean", kkBean);
			
			if(kkBean.getChouhyouShouninCd().equals("01") && kkBean.getMailAddr().equals(employee.getMailAddr())) 
				model.setViewName("/management/Commute/Kyuuka_Update");
			else 
				model.setViewName("/management/Commute/Kyuuka_Detail");
			return model;
		}
		catch(NullPointerException e) {
			return model;
		}
	}
	
	@RequestMapping(value=LinksConfig.KYUUKA_INFO_UPDATE_LINK, method=RequestMethod.POST)
	public String kyuukaUpdate(HttpServletRequest req, HttpSession session) {
		
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			String stDate = req.getParameter("kkaSdate");
			String edDate = req.getParameter("kkaEdate");
			
			KyuukaBean kkBean = new KyuukaBean();
			kkBean.setNo(Integer.parseInt(req.getParameter("kkaNo")));
			kkBean.setStartDate(stDate.substring(0, 4).concat(stDate.substring(5, 7).concat(stDate.substring(8, 10))));
			kkBean.setEndDate(edDate.substring(0, 4).concat(edDate.substring(5, 7).concat(edDate.substring(8, 10))));
			kkBean.setNaiyou(req.getParameter("kkaNaiyou"));
			kkBean.setYukisaki(req.getParameter("yukisaki"));
			kkBean.setHijouTel(req.getParameter("hijyoutel"));
			kkBean.setRiyuu(req.getParameter("kkaReason"));
			kkBean.setUpdateMail(employee.getMailAddr());
			kkBean.setUpdateDateTime(new Date());
			service.updateKyuuka(kkBean);
			
			return REDIRECT_KYUUKA_LIST;
		}
		catch(NullPointerException e) {
			return REDIRECT_LOGIN;
		}
	}
	
	@RequestMapping(value="/management/commute/kyuuka/delete", method=RequestMethod.POST)
	public String kyuukaDelete(HttpServletRequest req) {
		try {
			KyuukaBean kkBean = new KyuukaBean();
			kkBean.setNo(Integer.parseInt(req.getParameter("kkaNo")));
			service.deleteKyuuka(kkBean);
			
			return REDIRECT_KYUUKA_LIST;
		}
		catch(Exception e) {
			return REDIRECT_LOGIN;
		}
	}
	
	@RequestMapping(value="/management/commute/kyuuka/shounin/{kkNo}")
	public String kyuukaShounin(HttpSession session, @PathVariable int kkNo) {
		
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			KyuukaBean kkBean = new KyuukaBean();
			kkBean.setNo(kkNo);
			kkBean = service.getKyuukaDetail(kkBean);
			if(kkBean.getChouhyouShouninCd().equals("01") || kkBean.getChouhyouShouninCd().equals("05") 
					|| kkBean.getChouhyouShouninCd().equals("06")) {
				return "redirect:/management/commute/kyuukaDetail/" + kkNo;
			}
			else {
				if(null == kkBean.getLeaderShouninMailAddr() && employee.getLeaderKb().equals("02")) {
					kkBean.setChouhyouShouninCd("04");
					kkBean.setLeaderShouninMailAddr(employee.getMailAddr());
					kkBean.setLeaderShouninDateTime(new Date());
				}
				if(null == kkBean.getKanrisyaShouninMailAddr() && employee.getLeaderKb().equals("01")){
					kkBean.setChouhyouShouninCd("05");
					kkBean.setKanrisyaShouninMailAddr(employee.getMailAddr());
					kkBean.setKanrisyaShouninDateTime(new Date());
				}
				service.kyuukaKessai(kkBean);
				return "redirect:/management/commute/kyuukaDetail/" + kkNo;
			}
		}
		catch(NullPointerException e) {
			return LOCATION_LOGIN;
		}
	}

	
//	欠勤
	@RequestMapping(value=LinksConfig.KEKKIN_INFO_LINK, method=RequestMethod.GET)
	public ModelAndView goKekkin(HttpSession session) {
		
		ModelAndView model = new ModelAndView();
		if(null != session.getAttribute("commuteMsg")) {
			model.addObject("msg", session.getAttribute("commuteMsg"));
			session.removeAttribute("commuteMsg");
		}
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			model.addObject("loginUser", employee);
			model.addObject("empInfo", service.getEmployeeInfo(employee));
			
			SimpleDateFormat sdForm = new SimpleDateFormat("yyyyMMdd");
			model.addObject("sinseiDate", sdForm.format(new Date()));
			
			model.setViewName(LOCATION_KEKKIN);
			return model;
		}
		catch(NullPointerException e) {
			model.setViewName(LOCATION_LOGIN);
			return model;
		}
	}
	
	@RequestMapping(value="/management/commute/kekkin/sakusei", method=RequestMethod.POST)
	public String kekkinSakusei(HttpServletRequest req, HttpSession session) throws Exception {
		
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			String stDate = req.getParameter("kkSdate");
			String edDate = req.getParameter("kkEdate");
			
			KekkinBean kekBean = new KekkinBean();
			kekBean.setMailAddr(employee.getMailAddr());
			kekBean.setStartDate(stDate.substring(0, 4).concat(stDate.substring(5, 7).concat(stDate.substring(8, 10))));
			kekBean.setEndDate(edDate.substring(0, 4).concat(edDate.substring(5, 7).concat(edDate.substring(8, 10))));
			kekBean.setRiyuu(req.getParameter("kkReason"));
			SimpleDateFormat sdForm = new SimpleDateFormat("yyyyMMdd");
			kekBean.setSinseiDate(sdForm.format(new Date()));
			kekBean.setInsertMail(employee.getMailAddr());
			kekBean.setInsertDateTime(new Date());
			kekBean.setChouhyouShouninCd("01");
			kekBean.setKinntaiKb("06");
			service.insertKekkin(kekBean);
			
			return REDIRECT_KEKKIN_LIST;
		}
		catch(NullPointerException e) {
			return REDIRECT_LOGIN;
		}
	}
	
	@RequestMapping(value="/management/commute/kekkin/yousei", method=RequestMethod.POST)
	public String kekkinYousei(HttpSession session, HttpServletRequest req) {
		
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			
			if(null != req.getParameter("kkNo")) {
				KekkinBean delete = new KekkinBean();
				delete.setNo(Integer.parseInt(req.getParameter("kkNo")));
				service.deleteKekkin(delete);
			}
			String stDate = req.getParameter("kkSdate");
			String edDate = req.getParameter("kkEdate");
			
			KekkinBean kekBean = new KekkinBean();
			kekBean.setMailAddr(employee.getMailAddr());
			kekBean.setStartDate(stDate.substring(0, 4).concat(stDate.substring(5, 7).concat(stDate.substring(8, 10))));
			kekBean.setEndDate(edDate.substring(0, 4).concat(edDate.substring(5, 7).concat(edDate.substring(8, 10))));
			kekBean.setRiyuu(req.getParameter("kkReason"));
			SimpleDateFormat sdForm = new SimpleDateFormat("yyyyMMdd");
			kekBean.setSinseiDate(sdForm.format(new Date()));
			kekBean.setInsertMail(employee.getMailAddr());
			kekBean.setInsertDateTime(new Date());
			kekBean.setChouhyouShouninCd("02");
			kekBean.setKinntaiKb("06");
			service.insertKekkin(kekBean);
			
			return REDIRECT_KEKKIN_LIST;
		}
		catch(NullPointerException e) {
			return REDIRECT_LOGIN;
		}
	}
	
	@RequestMapping(value=LinksConfig.KEKKIN_INFO_LIST_LINK, method=RequestMethod.GET)
	public ModelAndView goKekkinList(HttpServletRequest req, HttpSession session) {
		
		ModelAndView model = new ModelAndView();
		if(null != session.getAttribute("commuteMsg")) {
			model.addObject("msg", session.getAttribute("commuteMsg"));
			session.removeAttribute("commuteMsg");
		}
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			ArrayList<Integer> yearsList = new ArrayList<Integer>();
			SimpleDateFormat yearForm = new SimpleDateFormat("yyyy");
			for(int i=2010; i<Integer.parseInt(yearForm.format(System.currentTimeMillis()))+1; i++ )
				yearsList.add(i);
			
			if(employee.getLeaderKb().equals(ENUM_KANRISYA.getValue()) || employee.getLeaderKb().equals(ENUM_GENBARIDA.getValue())) {
				model.addObject("empKubun", "kanrisya");
				model.setViewName(LOCATION_KEKKIN_LIST);
				KekkinBean kekBean = new KekkinBean();
				
				List<KekkinBean> list;
				String year = req.getParameter("searchYears");
				String month = req.getParameter("searchMonths");
				
				if(null != year || null != month) {
					kekBean.setStartDate(year.concat(month));
					list = service.getKekkinAllList(kekBean);
					model.addObject("searchYear", year);
					model.addObject("searchMonth", month);
				}
				else {
					SimpleDateFormat sdForm = new SimpleDateFormat(DATE_FORM_YM);
					String date = sdForm.format(System.currentTimeMillis());
					kekBean.setStartDate(date);
					list = service.getKekkinAllList(kekBean);
					model.addObject("searchYear", date.substring(0, 4));
					model.addObject("searchMonth", date.substring(4, 6));
				}
				model.addObject("list", list);
				model.addObject("yearsList", yearsList);
				return model;
			}
			else {
				model.setViewName(LOCATION_KEKKIN_LIST);
				
				KekkinBean kekBean = new KekkinBean();
				kekBean.setMailAddr(employee.getMailAddr());
				
				List<KekkinBean> list;
				String year = req.getParameter("searchYears");
				String month = req.getParameter("searchMonths");
				
				if(null != year || null != month) {
					kekBean.setStartDate(year.concat(month));
					list = service.getKekkinList(kekBean);
					model.addObject("searchYear", year);
					model.addObject("searchMonth", month);
				}
				else {
					SimpleDateFormat sdForm = new SimpleDateFormat(DATE_FORM_YM);
					String date = sdForm.format(System.currentTimeMillis());
					kekBean.setStartDate(date);
					list = service.getKekkinList(kekBean);
					model.addObject("searchYear", date.substring(0, 4));
					model.addObject("searchMonth", date.substring(4, 6));
				}
				model.addObject("list", list);
				model.addObject("yearsList", yearsList);
				return model;
			}
		}
		catch(NullPointerException e) {
			model.setViewName(LOCATION_LOGIN);
			return model;
		}
	}
	
	@RequestMapping(value="/management/commute/kekkinDetail/{kekNo}")
	public ModelAndView goKekkinDetail(@PathVariable int kekNo, HttpSession session) {
		
		ModelAndView model = new ModelAndView();
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			model.addObject("loginUser", employee);
			
			KekkinBean kekBean = new KekkinBean();
			kekBean.setNo(kekNo);
			kekBean = service.getKekkinDetail(kekBean);
			
			EmployeeBean sakuseiEmp = new EmployeeBean();
			sakuseiEmp.setMailAddr(kekBean.getMailAddr());
			model.addObject("empInfo", service.getEmployeeInfo(sakuseiEmp));
			SimpleDateFormat sdForm = new SimpleDateFormat("yyyyMMdd");
			model.addObject("sinseiDate", sdForm.format(kekBean.getInsertDateTime()));
			model.addObject("kkBean", kekBean);
			
			if(kekBean.getChouhyouShouninCd().equals("01") && kekBean.getMailAddr().equals(employee.getMailAddr())) 
				model.setViewName("/management/Commute/Kekkin_Update");
			else 
				model.setViewName("/management/Commute/Kekkin_Detail");
			return model;
		}
		catch(NullPointerException e) {
			return model;
		}
	}
	
	@RequestMapping(value=LinksConfig.KEKKIN_INFO_UPDATE_LINK, method=RequestMethod.POST)
	public String kekkinUpdate(HttpServletRequest req, HttpSession session) {
	
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			String stDate = req.getParameter("kkSdate");
			String edDate = req.getParameter("kkEdate");
			
			KekkinBean kekBean = new KekkinBean();
			kekBean.setNo(Integer.parseInt(req.getParameter("kkNo")));
			kekBean.setStartDate(stDate.substring(0, 4).concat(stDate.substring(5, 7).concat(stDate.substring(8, 10))));
			kekBean.setEndDate(edDate.substring(0, 4).concat(edDate.substring(5, 7).concat(edDate.substring(8, 10))));
			kekBean.setRiyuu(req.getParameter("kkReason"));
			kekBean.setUpdateMail(employee.getMailAddr());
			kekBean.setUpdateDateTime(new Date());
			service.updateKekkin(kekBean);
			
			return REDIRECT_KEKKIN_LIST;
		}
		catch(NullPointerException e) {
			return REDIRECT_LOGIN;
		}
	}
	
	@RequestMapping(value="/management/commute/kekkin/delete", method=RequestMethod.POST)
	public String kekkinDelete(HttpServletRequest req) {
		try {
			KekkinBean kkBean = new KekkinBean();
			kkBean.setNo(Integer.parseInt(req.getParameter("kkNo")));
			service.deleteKekkin(kkBean);
			
			return REDIRECT_KEKKIN_LIST;
		}
		catch(Exception e) {
			return REDIRECT_LOGIN;
		}
	}
	
	@RequestMapping(value="/management/commute/kekkin/shounin/{kekNo}")
	public String kekkinShounin(HttpSession session, @PathVariable int kekNo) {
		
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			KekkinBean kekBean = new KekkinBean();
			kekBean.setNo(kekNo);
			kekBean = service.getKekkinDetail(kekBean);
			if(kekBean.getChouhyouShouninCd().equals("01") || kekBean.getChouhyouShouninCd().equals("05") 
					|| kekBean.getChouhyouShouninCd().equals("06")) {
				return "redirect:/management/commute/kekkinDetail/" + kekNo;
			}
			else {
				if(null == kekBean.getLeaderShouninMailAddr() && employee.getLeaderKb().equals("02")) {
					kekBean.setChouhyouShouninCd("04");
					kekBean.setLeaderShouninMailAddr(employee.getMailAddr());
					kekBean.setLeaderShouninDateTime(new Date());
				}
				if(null == kekBean.getKanrisyaShouninMailAddr() && employee.getLeaderKb().equals("01")){
					kekBean.setChouhyouShouninCd("05");
					kekBean.setKanrisyaShouninMailAddr(employee.getMailAddr());
					kekBean.setKanrisyaShouninDateTime(new Date());
				}
				service.kekkinKessai(kekBean);
				return "redirect:/management/commute/kekkinDetail/" + kekNo;
			}
		}
		catch(NullPointerException e) {
			return LOCATION_LOGIN;
		}
	}
	
	
//	返却
	@RequestMapping(value="/management/commute/hennkyaku")
	public ModelAndView goHennkyaku(HttpSession session, @RequestParam String kinntaiKb, @RequestParam int no) {
		
		ModelAndView model = new ModelAndView();
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			model.addObject("loginUser", employee);
			model.addObject("kinntaiKb", kinntaiKb);
			model.addObject("no", no);
			
			if(kinntaiKb.equals("01")) {
				ChikokuBean ckBean = new ChikokuBean();
				ckBean.setNo(no);
				model.addObject("commuteBean", service.getChikokuDetail(ckBean));
			}
			else if(kinntaiKb.equals("02")) {
				GaisyutsuBean gsBean = new GaisyutsuBean();
				gsBean.setNo(no);
				model.addObject("commuteBean", service.getGaisyutsuDetail(gsBean));
			}
			else if(kinntaiKb.equals("03")) {
				SoutaiBean stBean = new SoutaiBean();
				stBean.setNo(no);
				model.addObject("commuteBean", service.getSoutaiDetail(stBean));
			}
			else if(kinntaiKb.equals("04")) {
				HankyuuBean hkBean = new HankyuuBean();
				hkBean.setNo(no);
				model.addObject("commuteBean", service.getHankyuuDetail(hkBean));
			}
			else if(kinntaiKb.equals("05")) {
				KyuukaBean kkBean = new KyuukaBean();
				kkBean.setNo(no);
				model.addObject("commuteBean", service.getKyuukaDetail(kkBean));
			}
			else {
				KekkinBean kekBean = new KekkinBean();
				kekBean.setNo(no);
				model.addObject("commuteBean", service.getKekkinDetail(kekBean));
			}
			model.setViewName("/management/Commute/henkyaku");
			return model;
		}
		catch(NullPointerException e) {
			model.setViewName(LOCATION_LOGIN);
			return model;
		}
	}
	
	@RequestMapping(value="/management/commute/hennkyaku/action")
	public String hennkyakuAction(HttpSession session, HttpServletRequest req) {
		
		try {
			EmployeeBean employee = (EmployeeBean)session.getAttribute("employee");
			
			String kinntaiKb = req.getParameter("kinntaiKb");
			int no = Integer.parseInt(req.getParameter("no"));
			String hennkyakuReason = req.getParameter("hennkyakuReason");
			
			if(kinntaiKb.equals("01")) {
				ChikokuBean ckBean = new ChikokuBean();
				ckBean.setNo(no);
				ckBean = service.getChikokuDetail(ckBean);
				
				ckBean.setChouhyouShouninCd("06");
				ckBean.setLeaderShouninMailAddr(null);
				ckBean.setLeaderShouninDateTime(null);
				ckBean.setKanrisyaShouninMailAddr(null);
				ckBean.setKanrisyaShouninDateTime(null);
				ckBean.setHennkyakuMailAddr(employee.getMailAddr());
				ckBean.setHennkyakuDateTime(new Date());
				ckBean.setHennkyakuReason(hennkyakuReason);
				service.chikokuHennkyaku(ckBean);
			}
			else if(kinntaiKb.equals("02")) {
				GaisyutsuBean gsBean = new GaisyutsuBean();
				gsBean.setNo(no);
				gsBean = service.getGaisyutsuDetail(gsBean);
				
				gsBean.setChouhyouShouninCd("06");
				gsBean.setLeaderShouninMailAddr(null);
				gsBean.setLeaderShouninDateTime(null);
				gsBean.setKanrisyaShouninMailAddr(null);
				gsBean.setKanrisyaShouninDateTime(null);
				gsBean.setHennkyakuMailAddr(employee.getMailAddr());
				gsBean.setHennkyakuDateTime(new Date());
				gsBean.setHennkyakuReason(hennkyakuReason);
				service.gaisyutsuHennkyaku(gsBean);
			}
			else if(kinntaiKb.equals("03")) {
				SoutaiBean stBean = new SoutaiBean();
				stBean.setNo(no);
				stBean = service.getSoutaiDetail(stBean);
				
				stBean.setChouhyouShouninCd("06");
				stBean.setLeaderShouninMailAddr(null);
				stBean.setLeaderShouninDateTime(null);
				stBean.setKanrisyaShouninMailAddr(null);
				stBean.setKanrisyaShouninDateTime(null);
				stBean.setHennkyakuMailAddr(employee.getMailAddr());
				stBean.setHennkyakuDateTime(new Date());
				stBean.setHennkyakuReason(hennkyakuReason);
				service.soutaiHennkyaku(stBean);
			}
			else if(kinntaiKb.equals("04")) {
				HankyuuBean hkBean = new HankyuuBean();
				hkBean.setNo(no);
				hkBean = service.getHankyuuDetail(hkBean);
				
				hkBean.setChouhyouShouninCd("06");
				hkBean.setLeaderShouninMailAddr(null);
				hkBean.setLeaderShouninDateTime(null);
				hkBean.setKanrisyaShouninMailAddr(null);
				hkBean.setKanrisyaShouninDateTime(null);
				hkBean.setHennkyakuMailAddr(employee.getMailAddr());
				hkBean.setHennkyakuDateTime(new Date());
				hkBean.setHennkyakuReason(hennkyakuReason);
				service.hankyuuHennkyaku(hkBean);
			}
			else if(kinntaiKb.equals("05")) {
				KyuukaBean kkBean = new KyuukaBean();
				kkBean.setNo(no);
				kkBean = service.getKyuukaDetail(kkBean);
				
				kkBean.setChouhyouShouninCd("06");
				kkBean.setLeaderShouninMailAddr(null);
				kkBean.setLeaderShouninDateTime(null);
				kkBean.setKanrisyaShouninMailAddr(null);
				kkBean.setKanrisyaShouninDateTime(null);
				kkBean.setHennkyakuMailAddr(employee.getMailAddr());
				kkBean.setHennkyakuDateTime(new Date());
				kkBean.setHennkyakuReason(hennkyakuReason);
				service.kyuukaHennkyaku(kkBean);
			}
			else {
				KekkinBean kekBean = new KekkinBean();
				kekBean.setNo(no);
				kekBean = service.getKekkinDetail(kekBean);
				
				kekBean.setChouhyouShouninCd("06");
				kekBean.setLeaderShouninMailAddr(null);
				kekBean.setLeaderShouninDateTime(null);
				kekBean.setKanrisyaShouninMailAddr(null);
				kekBean.setKanrisyaShouninDateTime(null);
				kekBean.setHennkyakuMailAddr(employee.getMailAddr());
				kekBean.setHennkyakuDateTime(new Date());
				kekBean.setHennkyakuReason(hennkyakuReason);
				service.kekkinHennkyaku(kekBean);
			}
			return "redirect:/management/commute/hennkyaku/complete";
		}
		catch(NullPointerException e) {
			return LOCATION_LOGIN;
		}
	}
	
	@RequestMapping(value="/management/commute/hennkyaku/complete")
	public ModelAndView hennkyakuComplete() {
		
		 ModelAndView model = new ModelAndView();
		 
		 model.setViewName("/management/Commute/henkyakuComplete");
		 return model;
	}

}
