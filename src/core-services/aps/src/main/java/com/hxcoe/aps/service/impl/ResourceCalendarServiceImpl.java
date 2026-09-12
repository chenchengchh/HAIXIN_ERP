package com.hxcoe.aps.service.impl;
import com.hxcoe.aps.entity.ResourceCalendarEntity;
import com.hxcoe.aps.repository.ResourceCalendarRepository;
import com.hxcoe.aps.service.ResourceCalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class ResourceCalendarServiceImpl implements ResourceCalendarService {

    @Autowired
    private ResourceCalendarRepository resourceCalendarRepository;

    @Override
    public List<ResourceCalendarEntity> getAllResourceCalendars() {
        return resourceCalendarRepository.findAll();
    }

    @Override
    public ResourceCalendarEntity createResourceCalendar(ResourceCalendarEntity resourceCalendar) {
        return resourceCalendarRepository.save(resourceCalendar);
    }

    @Override
    public ResourceCalendarEntity getResourceCalendarById(Long id) {
        return resourceCalendarRepository.findById(id).orElse(null);
    }

    @Override
    public List<ResourceCalendarEntity> getResourceCalendarsByResourceId(Long resourceId) {
        return resourceCalendarRepository.findByResourceId(resourceId);
    }

    @Override
    public List<ResourceCalendarEntity> getResourceCalendarsByResourceIdAndDateRange(Long resourceId, LocalDate startDate, LocalDate endDate) {
        return resourceCalendarRepository.findByResourceIdAndDateBetween(resourceId, startDate, endDate);
    }

    @Override
    public ResourceCalendarEntity updateResourceCalendar(Long id, ResourceCalendarEntity resourceCalendar) {
        return resourceCalendarRepository.save(resourceCalendar);
    }

    @Override
    public void deleteResourceCalendar(Long id) {
        resourceCalendarRepository.deleteById(id);
    }

    @Override
    public List<ResourceCalendarEntity> batchSetResourceCalendars(List<ResourceCalendarEntity> resourceCalendars) {
        return resourceCalendarRepository.saveAll(resourceCalendars);
    }

    @Override
    public boolean isResourceAvailableOnDate(Long resourceId, LocalDate date) {
        ResourceCalendarEntity calendar = resourceCalendarRepository.findByResourceIdAndDate(resourceId, date);
        return calendar != null && calendar.getAvailable();
    }

    @Override
    public Integer getAvailableMinutesOnDate(Long resourceId, LocalDate date) {
        ResourceCalendarEntity calendar = resourceCalendarRepository.findByResourceIdAndDate(resourceId, date);
        if (calendar == null || !calendar.getAvailable()) {
            return 0;
        }
        return (int) (calendar.getAvailableHours() * 60);
    }
}
