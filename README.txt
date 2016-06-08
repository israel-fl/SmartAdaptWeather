#SmartAdapt Weather

© 2016 Israel Flores

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

SmartAdapt Weather is an app that was built as a tool in building architecture. It is meant to provide information to the user based on values received from both the outside weather and the current status inside of the building. This values include Temperature, wind, CO2 levels, and humidity. 

By using an API to connect to OpenWeather, the app retrieves weather information live from the website and displays it for the user. Then, using a custom built device, a LabView script, a powershell script, and a python script, weather data from inside the building is then pushed into a Parse database. The app then retrieves this second set of values and displays them. 

Using a formula called Predicted Mean Voter (PMV) the app then uses both the outside and inside weather information to calculate the PMV range. This range is the predicted "comfort" level at which most people will be satisfied with temperature inside the building. This is calculated using estimations for surface temperature of clothing, irradiated body heat, CO2 levels, thickness of clothing, and a multitude of other factors.

If the PMV false outside of the comfort range, then the user is provided a suggestion. The suggestions are something like: "Wear Lighter Clothes", or "Open a window", then it shows the estimated monetary savings if a user follows the suggestions for a month.

The user may also elect to ignore the suggestion, which will increase the PMV range and "intelligently" adapt the formula so that the user gets better suggestions. 


****Known Bugs****

As of right now, the unit conversion does NOT work. I didn't fix it because there were other things that were more relevant.

Pressing the back arrow from settings causes the app to crash, again, not a major problem since you can use the back button on the device to go back.

if you ignore and change the PMV boundaries from -0.5 and 0.5 to -0.8 and 0.8, etc the values reset when you kill the app and start it again. Since I didn't add a way for you to physically adjust the limits I figured this was okay. That way if you press ignore by accident and change the limits, you just have to restart the app.

Sometimes the PMV gets calculated wrong, all you have to do is pull down to REFRESH. Once should work, but you can do it until the values look right.
