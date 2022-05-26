
public class KinmuController {
	
}
	// 勤務表画面
//	@RequestMapping(value = LinksConfig.PRINT_KINMUHYOU_LINK, method = RequestMethod.GET)
//	public ModelAndView kinmuhyou(HttpSession session) throws Exception {
//		EmployeeBean emp = (EmployeeBean) session.getAttribute(EMPLOYEE);
//		try {
//			if(null != emp) {
//				model = new ModelAndView(PRINT + SLASH + WORK);
//				if(emp.getLeaderKb().equals(ENUM_KANRISYA.getValue()) || emp.getLeaderKb().equals(ENUM_GENBARIDA.getValue())) {
//					model.addObject(KANRISHA_KUBUN, ENUM_KANRISYA.getValue());
//					model.addObject(EMP_KUBUN, ENUM_GENBARIDA.getValue());
//					return model;
//				}
//				loginUser = emp.getMailAddr();
//				userList = printService.selectUser(loginUser);
//				tsuukinnList = printService.selectTsuukinn(loginUser);
//				kekkinnList = printService.selectKekkinn(loginUser);
//				kyuukaList = printService.selectKyuuka(loginUser);
//				shuttaikinnList = printService.selectShuttaikinn(loginUser);
//				model.addObject(USER_LIST, userList);
//				model.addObject(TSUUKINN_LIST, tsuukinnList);
//				model.addObject(KEKKINN_LIST, kekkinnList);
//				model.addObject(KYUUKA_LIST, kyuukaList);
//				model.addObject(SHUTTAIKINN_LIST, shuttaikinnList);
//				return model;
//			}else {
//				return loginCon.goLogin(session);
//			}
//		}catch(Exception e) {
//			e.printStackTrace();
//			return loginCon.goLogin(session);
//		}
//	}
//
//	// 勤務表画面で出力ボタンが押下された場合の処理
//	@RequestMapping(value = LinksConfig.PRINT_KINMUHYOU_LINK, method = RequestMethod.POST)
//	public ModelAndView kinmuhyou(HttpSession session, HttpServletRequest request) throws Exception {
//		EmployeeBean emp = (EmployeeBean) session.getAttribute(EMPLOYEE);
//		try {
//			if(null != emp) {
//				week = new ArrayList<String>();
//				
//				// 勤務表画面のカレンダーの情報をセット
//				name = request.getParameter(NAME);
//				firstLabel = request.getParameter(FIRST_LABEL);
//				valYear = request.getParameter(GET_NOW).split(DASH)[0];
//				valMonth = request.getParameter(GET_NOW).split(DASH)[1];
//				
//				model = new ModelAndView(PRINT + SLASH + WORK);
//				loginUser = emp.getMailAddr();
//				userList = printService.selectUser(loginUser);
//				tsuukinnList = printService.selectTsuukinn(loginUser);
//				kekkinnList = printService.selectKekkinn(loginUser);
//				kyuukaList = printService.selectKyuuka(loginUser);
//				shuttaikinnList = printService.selectShuttaikinn(loginUser);
//				
//				// エクセルファイルに出力
//				try {
//					
//					// エクセルファイルの最初の曜日をセット
//					for (int i = 0; i < LABEL.length; i++) {
//						if (firstLabel.equals(LABEL[i]) == true) {
//							check = i;
//						}
//					}
//					
//					// 帳票を出力日付を計算
//					for (int i = 0; i < request.getParameter(SYSDATE).split(BIG_SPACE).length; i++) {
//						if (i == 2) {
//							year = request.getParameter(SYSDATE).split(BIG_SPACE)[i].split(NENN)[0];
//						}
//					}
//					
//					// 勤務表をエクセルで出力ための設定
//					fileIn = new FileInputStream(PATH.getAbsolutePath() + EXCEL + SLASH + KINNMU + XLSX);
//					workbook = new XSSFWorkbook(fileIn);
//					cellStyle = workbook.createCellStyle();
//					sheet = workbook.getSheetAt(0);
//					XSSFPrintSetup print = sheet.getPrintSetup();
//					print.setScale( (short)90 );
//					cellStyle.setVerticalAlignment(VerticalAlignment.TOP);
//					sheet.protectSheet(ROCK);
//					sheet.getRow(4).getCell(3).setCellValue(name);
//					sheet.getRow(1).getCell(5).setCellValue(BIG_SPACE + year + NENN + request.getParameter(GET_NOW).split(DASH)[1] + KINNMU);
//
//					// 通勤情報の出勤時間、退勤時間を元に基本時間、休憩時間を計算し、エクセルに出力
//					workTime = 0;
//					for (int i = 0; i < tsuukinnList.size(); i++) {
//						tYear = tsuukinnList.get(i).getDate().split(NENN)[0];
//						tMonth = tsuukinnList.get(i).getDate().split(NENN)[1].split(GATSU)[0];
//						
//						// カレンダーの年月と通勤履歴を比較して一致情報のみを選択し計算
//						if(tYear.equals(valYear) == true && tMonth.equals(valMonth) == true) {	
//							tsuukinnDay = Integer.parseInt(tsuukinnList.get(i).getDate().split(GATSU)[1].split(HI)[0]);
//							
//							// 通勤時間を15分単位でセット
//							if (null != tsuukinnList.get(i).getSyukinTime()) {
//								shukkinnHour = Integer.parseInt(tsuukinnList.get(i).getSyukinTime().split(JI)[0]);
//								shukkinnMin = Integer.parseInt(tsuukinnList.get(i).getSyukinTime().split(JI)[1].split(HUN)[0]);
//								if(shukkinnMin > 45){
//									shukkinnHour += 1;
//									shukkinnMin = 0;
//								}else if(shukkinnMin > 30) {
//									shukkinnMin = 45;
//								}else if(shukkinnMin >= 15) {
//									shukkinnMin = 30;
//								}else if(shukkinnMin >= 1){
//									shukkinnMin = 15;
//								}
//								sheet.getRow(7 + tsuukinnDay).getCell(2).setCellValue(shukkinnHour);
//								sheet.getRow(7 + tsuukinnDay).getCell(3).setCellValue(shukkinnMin);
//							}
//							
//							// 退勤時間を15分単位でセット
//							if (null != tsuukinnList.get(i).getTaikinTime()) {
//								taikinnHour = Integer.parseInt(tsuukinnList.get(i).getTaikinTime().split(JI)[0]);
//								taikinnMin = Integer.parseInt(tsuukinnList.get(i).getTaikinTime().split(JI)[1].split(HUN)[0]);
//								if(taikinnMin > 45){
//									taikinnHour += 1;
//									taikinnMin = 0;
//								}else if(taikinnMin > 30) {
//									taikinnMin = 45;
//								}else if(taikinnMin >= 15) {
//									taikinnMin = 30;
//								}else if(taikinnMin >= 1){
//									taikinnMin = 15;
//								}
//								sheet.getRow(7 + tsuukinnDay).getCell(4).setCellValue(taikinnHour);
//								sheet.getRow(7 + tsuukinnDay).getCell(5).setCellValue(taikinnMin);
//							}
//							
//							// 出勤時間、退勤時間を元に休憩時間をセット
//							if(null != tsuukinnList.get(i).getSyukinTime() && null != tsuukinnList.get(i).getTaikinTime()) {
//								
//								// 通勤履歴の出勤時間、退勤時間の間、12から13が含まれると休憩時間を1時間にセット
//								if(shukkinnHour < 12 && 13 < taikinnHour) {
//									kyuukei = ONE;
//									if(shukkinnMin > taikinnMin) {
//										sheet.getRow(7 + tsuukinnDay).getCell(7).setCellValue(taikinnHour - shukkinnHour - 1.5);
//										workTime += taikinnHour - shukkinnHour - 1.5;
//									}else {
//										sheet.getRow(7 + tsuukinnDay).getCell(7).setCellValue(taikinnHour - shukkinnHour - 1.0);
//										workTime += taikinnHour - shukkinnHour - 1.0;
//									}
//									sheet.getRow(7 + tsuukinnDay).getCell(9).setCellValue(kyuukei);
//									
//								// そうでなければ0時間にセット
//								}else {
//									kyuukei = ZERO;
//									if(shukkinnMin > taikinnMin) {
//										sheet.getRow(7 + tsuukinnDay).getCell(7).setCellValue(taikinnHour - shukkinnHour- 0.5);
//										workTime += taikinnHour - shukkinnHour- 0.5;
//									}else {
//										sheet.getRow(7 + tsuukinnDay).getCell(7).setCellValue(taikinnHour - shukkinnHour);
//										workTime += taikinnHour - shukkinnHour;
//									}
//									sheet.getRow(7 + tsuukinnDay).getCell(9).setCellValue(kyuukei);
//								}
//								
//								// 通勤時間と退勤時間の差を計算し、30分単位で計算基本時間をセット
//								if((shukkinnHour == taikinnHour) && (taikinnMin - shukkinnMin) >= 30) {
//									sheet.getRow(7 + tsuukinnDay).getCell(7).setCellValue(taikinnHour - shukkinnHour + 0.5);
//									workTime += taikinnHour - shukkinnHour + 0.5;
//								}
//							}
//							
//							// 通勤情報を元に残業時間をセット
//							if (null != tsuukinnList.get(i).getZangyouJikan()) {
//								sheet.getRow(7 + tsuukinnDay).getCell(10).setCellValue(tsuukinnList.get(i).getZangyouJikan());
//							}
//							sheet.getRow(42).getCell(7).setCellValue(workTime);
//						}
//					}
//					
//					// 出退勤情報の遅刻、外出、早退、半休の情報をセット
//					for(int i = 0; i < shuttaikinnList.size(); i++) {
//						if(null != shuttaikinnList.get(i).getDate()) {
//							sYear = shuttaikinnList.get(i).getDate().substring(0, 4);
//							sMonth = shuttaikinnList.get(i).getDate().substring(4).substring(0, 2);
//							// カレンダーの年月と出退勤履歴を比較して一致情報のみを選択し計算
//							if(null != request.getParameter(BLANK_LIST + i) && sYear.equals(valYear) == true && sMonth.equals(valMonth) == true) {
//								temp = Integer.parseInt(request.getParameter(BLANK_LIST + i).split(TENN)[0]) + 7;
//								if(request.getParameter(BLANK_LIST + i).length() > 3) {
//									sheet.getRow(temp).getCell(14).setCellValue(request.getParameter(BLANK_LIST + i).split(TENN)[1]);
//								}
//							} 
//						}
//					}
//					
//					// 欠勤情報をセット
//					for(int i = 0; i < kekkinnList.size(); i++) {
//						
//						//　欠勤が始まる日をセット
//						if(null != kekkinnList.get(i).getKekkinnStart()) {
//							sYear = kekkinnList.get(i).getKekkinnStart().substring(0, 4);
//							sMonth = kekkinnList.get(i).getKekkinnStart().substring(4).substring(0, 2);
//							sDay = kekkinnList.get(i).getKekkinnStart().substring(6);
//						}
//						
//						//　欠勤が終わるまでの日をセット
//						if(null != kekkinnList.get(i).getKekkinnEnd()) {
//							eDay = kekkinnList.get(i).getKekkinnEnd().substring(6);
//						}
//						
//						// カレンダーの年月と欠勤履歴を比較して一致情報のみを選択し、計算
//						if(null != request.getParameter(BLANK_KEKKINN + i) && sYear.equals(valYear) == true && sMonth.equals(valMonth) == true) {
//							if(sDay == eDay) {
//								sheet.getRow(Integer.parseInt(sDay) + 7).getCell(14).setCellValue(request.getParameter(BLANK_KEKKINN + i).split(SPACE)[1]);
//							}else {
//								for(int j = Integer.parseInt(sDay); j < Integer.parseInt(eDay)+1; j++) {
//									sheet.getRow(j + 7).getCell(14).setCellValue(request.getParameter(BLANK_KEKKINN + i).split(SPACE)[1]);
//								}
//							}
//						} 
//					}
//					
//					// 休暇情報をセット
//					for(int i = 0; i < kyuukaList.size(); i++) {
//						
//						//　休暇が始まる日をセット
//						if(null != kyuukaList.get(i).getKyuukaStart()) {
//							sYear = kyuukaList.get(i).getKyuukaStart().substring(0, 4);
//							sMonth = kyuukaList.get(i).getKyuukaStart().substring(4).substring(0, 2);
//							sDay = kyuukaList.get(i).getKyuukaStart().substring(6);
//						}
//						
//						//　休暇が終わるまでの日をセット
//						if(null != kyuukaList.get(i).getKyuukaEnd()) {
//							eDay = kyuukaList.get(i).getKyuukaEnd().substring(6);
//						}
//						
//						// カレンダーの年月と休暇履歴を比較して一致情報のみを選択し計算
//						if(null != request.getParameter(BLANK_KYUUKA + i) && sYear.equals(valYear) == true && sMonth.equals(valMonth) == true) {
//							if(sDay == eDay) {
//								sheet.getRow(Integer.parseInt(sDay) + 8).getCell(14).setCellValue(request.getParameter(BLANK_KYUUKA + i).split(SPACE)[1]);
//							}else {
//								for(int j = Integer.parseInt(sDay); j < Integer.parseInt(eDay)+1; j++) {
//									sheet.getRow(j + 7).getCell(14).setCellValue(request.getParameter(BLANK_KYUUKA + i).split(SPACE)[1]);
//								}
//							}
//						} 
//					} 
//					
//					// エクセルに表示される一ヶ月分の日をセット
//					for (int i = 1; i <= Integer.parseInt(request.getParameter(KIHON_DAY)) + Integer.parseInt(request.getParameter(HOLY_DAY)); i++) {
//						sheet.getRow(7 + i).getCell(0).setCellValue(i);
//					}
//					
//					// エクセルに表示される一ヶ月分の日に合わせ曜日をセット
//					for (int i = 0; i < Integer.parseInt(request.getParameter(KIHON_DAY)) + Integer.parseInt(request.getParameter(HOLY_DAY)); i++) {
//						if(check < 6) {
//							week.add(LABEL[check]);
//							check++;
//						}else {
//							week.add(LABEL[check]);
//							check = 0;
//						}
//					}
//					
//					// エクセルに表示される曜日のが「土曜」又は「日曜」なら色をセット
//					for (int i = 0; i < Integer.parseInt(request.getParameter(KIHON_DAY)) + Integer.parseInt(request.getParameter(HOLY_DAY)); i++) {
//						cellStyle = workbook.createCellStyle();
//						font = workbook.createFont();
//						font.setFontName(FONT);
//						font.setFontHeightInPoints((short)16);
//						font.setColor(IndexedColors.WHITE.getIndex());
//						font.setBold(true);
//						cellStyle.setFont(font);
//						cellStyle.setAlignment(HorizontalAlignment.CENTER);
//						cellStyle.setBorderTop(BorderStyle.HAIR);
//						cellStyle.setBorderBottom(BorderStyle.HAIR);
//						cellStyle.setBorderLeft(BorderStyle.THIN);
//						cellStyle.setBorderRight(BorderStyle.THIN);
//						
//						// 曜日のが「土曜」なら青色
//						if(week.get(i) == "土") {
//							cellStyle.setFillForegroundColor(IndexedColors.BLUE.index);
//						    cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//						    sheet.getRow(8 + i).getCell(1).setCellStyle(cellStyle);
//						}
//						
//						// 曜日のが「日曜」なら赤色
//						if(week.get(i) == HI) {
//							cellStyle.setFillForegroundColor(IndexedColors.RED.index);
//						    cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//						    sheet.getRow(8 + i).getCell(1).setCellStyle(cellStyle);
//						}
//						sheet.getRow(8 + i).getCell(1).setCellValue(week.get(i));
//					}
//					
//					// エクセルの下段の情報をセット
//					sheet.getRow(43).getCell(10).setCellValue(request.getParameter(KIHON_DAY));
//					sheet.getRow(43).getCell(11).setCellValue(request.getParameter(WORK_DAY));
//					sheet.getRow(43).getCell(12).setCellValue(request.getParameter(HOLY_DAY));
//					sheet.getRow(43).getCell(13).setCellValue(request.getParameter(STOP_DAY));
//					sheet.getRow(47).getCell(10).setCellValue(request.getParameter(KIHON_TIME));
//					sheet.getRow(47).getCell(11).setCellValue(request.getParameter(OVER_TIME));
//					sheet.getRow(47).getCell(12).setCellValue(request.getParameter(LATE_DAY));
//					sheet.getRow(47).getCell(13).setCellValue(request.getParameter(SPEED_DAY));
//					sheet.getRow(47).getCell(14).setCellValue(request.getParameter(HALF_DAY));
//					
//					StampBean stampBean = service.selectStamp(loginUser);
//					
//					byte[] bytes = stampBean.getFile();
//					int picIdx = workbook.addPicture(bytes, XSSFWorkbook.PICTURE_TYPE_PNG);
//					XSSFCreationHelper helper = workbook.getCreationHelper();
//					XSSFDrawing drawing = sheet.createDrawingPatriarch();
//					XSSFClientAnchor anchor = helper.createClientAnchor();
//				    
//					anchor.setRow1(4);
//					anchor.setCol1(7);
//				
//					CellStyle style= workbook.createCellStyle();
//
//				    sheet.getRow(4).getCell(7).setCellStyle(style);
//					
//					 XSSFPicture pic = drawing.createPicture(anchor, picIdx);
//					 pic.resize(1, 1);
//					
//					// 出力
//					fileOut = new FileOutputStream(SAVE_PATH + SLASH + request.getParameter(GET_NOW).split(DASH)[1] + TSUKI_KINNMU + name + FILE_END + P_COUNT + XLSX);
//					P_COUNT++;
//				} catch (Exception e) {
//					
//					// 問題が発症したら帳票選択画面に戻る
//					e.printStackTrace();
//					return loginCon.goLogin(session);
//				} finally {
//					workbook.write(fileOut);
//					workbook.close();
//					fileIn.close();
//					fileOut.close();
//				}
//
//				model.addObject(USER_LIST, userList);
//				model.addObject(TSUUKINN_LIST, tsuukinnList);
//				model.addObject(KEKKINN_LIST, kekkinnList);
//				model.addObject(KYUUKA_LIST, kyuukaList);
//				model.addObject(SHUTTAIKINN_LIST, shuttaikinnList);
//				return model;
//			}else {
//				return loginCon.goLogin(session);
//			}
//		}catch(Exception e) {
//			e.printStackTrace();
//			return loginCon.goLogin(session);
//		}
//	}
//	
//	// work
//	@RequestMapping(value = "/print/work", method = RequestMethod.GET)
//	public ModelAndView work(HttpSession session, HttpServletRequest request) throws Exception {
//		model = new ModelAndView("print/work");
//		return model;
//	}
